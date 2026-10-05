package portal.expenses.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import portal.expenses.dto.PageResponse;
import portal.expenses.dto.UserCreateRequest;
import portal.expenses.dto.UserResponseDto;
import portal.expenses.entity.AppUser;
import portal.expenses.entity.Role;
import portal.expenses.repository.RoleRepository;
import portal.expenses.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void testGetAllUsers() {
        Role role = new Role();
        role.setName("ROLE_USER");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("john");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setRoles(Set.of(role));

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("john", result.get(0).username());
        assertEquals("John Doe", result.get(0).name());
        assertEquals("john@example.com", result.get(0).email());
        assertTrue(result.get(0).roles().contains("ROLE_USER"));
    }

    @Test
    void testCreateUser_Success() {
        UserCreateRequest request = new UserCreateRequest("alice", "secret", "Alice Smith", "alice@example.com", "ROLE_ADMIN");

        Role role = new Role();
        role.setName("ROLE_ADMIN");

        when(passwordEncoder.encode("secret")).thenReturn("encodedSecret");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(role));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserResponseDto result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals("alice", result.username());
        assertEquals("Alice Smith", result.name());
        assertEquals("alice@example.com", result.email());
        assertTrue(result.roles().contains("ROLE_ADMIN"));
        verify(passwordEncoder).encode("secret");
        verify(roleRepository).findByName("ROLE_ADMIN");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void testCreateUser_RoleNotFound() {
        UserCreateRequest request = new UserCreateRequest("bob", "pass", "Bob", "bob@example.com", "ROLE_UNKNOWN");

        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(request));
        assertEquals("Role not found", ex.getMessage());
    }

    @Test
    void testDeleteUser() {
        Long userId = 5L;
        userService.deleteUser(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void testGetAllUsersWithPagination_Ascending() {
        Role role = new Role();
        role.setName("ROLE_USER");

        AppUser user = new AppUser();
        user.setId(2L);
        user.setUsername("user2");
        user.setName("User Two");
        user.setEmail("user2@example.com");
        user.setRoles(Set.of(role));

        Page<AppUser> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 10, "username", "ASC");

        assertNotNull(response);
    }

    @Test
    void testGetAllUsersWithPagination_Descending() {
        Page<AppUser> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(1, 5, "id", "DESC");

        assertNotNull(response);
    }
}