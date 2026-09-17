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
        user.setUsername("testuser");
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRoles(Set.of(role));

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("testuser", result.get(0).username());
        assertEquals("Test User", result.get(0).name());
        assertEquals("test@example.com", result.get(0).email());
        assertTrue(result.get(0).roles().contains("ROLE_USER"));
    }

    @Test
    void testCreateUser_Success() {
        UserCreateRequest request = new UserCreateRequest("newuser", "password123", "New User", "new@example.com", "ROLE_USER");

        Role role = new Role();
        role.setName("ROLE_USER");

        AppUser savedUser = new AppUser();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setPassword("encodedPassword");
        savedUser.setName("New User");
        savedUser.setEmail("new@example.com");
        savedUser.setRoles(Set.of(role));

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        UserResponseDto result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("newuser", result.username());
        assertEquals("New User", result.name());
        assertEquals("new@example.com", result.email());
        assertTrue(result.roles().contains("ROLE_USER"));
    }

    @Test
    void testCreateUser_RoleNotFound() {
        UserCreateRequest request = new UserCreateRequest("newuser", "password123", "New User", "new@example.com", "ROLE_UNKNOWN");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(request));
        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testGetAllUsersWithPagination_Desc() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        AppUser user = new AppUser();
        user.setId(3L);
        user.setUsername("admin");
        user.setName("Admin User");
        user.setEmail("admin@example.com");
        user.setRoles(Set.of(role));

        Page<AppUser> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> result = userService.getAllUsersWithPagination(0, 10, "id", "DESC");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(3L, result.getContent().get(0).id());
        assertEquals("admin", result.getContent().get(0).username());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
    }

    @Test
    void testGetAllUsersWithPagination_Asc() {
        Page<AppUser> page = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> result = userService.getAllUsersWithPagination(0, 10, "username", "ASC");

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
}