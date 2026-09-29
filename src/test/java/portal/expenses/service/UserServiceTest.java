package portal.expenses.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        assertEquals(1L, result.get(0).id());
        assertEquals("john", result.get(0).username());
        assertEquals("John Doe", result.get(0).name());
        assertEquals("john@example.com", result.get(0).email());
        assertTrue(result.get(0).roles().contains("ROLE_USER"));
    }

    @Test
    void testCreateUser_Success() {
        UserCreateRequest request = new UserCreateRequest("jane", "secret123", "Jane Doe", "jane@example.com", "ROLE_ADMIN");

        Role role = new Role();
        role.setName("ROLE_ADMIN");

        when(passwordEncoder.encode("secret123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(role));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        UserResponseDto result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("jane", result.username());
        assertEquals("Jane Doe", result.name());
        assertEquals("jane@example.com", result.email());
        assertTrue(result.roles().contains("ROLE_ADMIN"));
        verify(passwordEncoder).encode("secret123");
        verify(roleRepository).findByName("ROLE_ADMIN");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void testCreateUser_RoleNotFound() {
        UserCreateRequest request = new UserCreateRequest("jane", "secret123", "Jane Doe", "jane@example.com", "ROLE_NONEXISTENT");

        when(passwordEncoder.encode("secret123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_NONEXISTENT")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(request));
        assertEquals("Role not found", ex.getMessage());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void testGetAllUsersWithPagination_DescSort() {
        Role role = new Role();
        role.setName("ROLE_USER");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("john");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setRoles(Set.of(role));

        Page<AppUser> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> result = userService.getAllUsersWithPagination(0, 10, "username", "DESC");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("john", result.getContent().get(0).username());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void testGetAllUsersWithPagination_AscSort() {
        Role role = new Role();
        role.setName("ROLE_USER");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("john");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setRoles(Set.of(role));

        Page<AppUser> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> result = userService.getAllUsersWithPagination(0, 10, "username", "ASC");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("john", result.getContent().get(0).username());
        verify(userRepository).findAll(any(Pageable.class));
    }
}