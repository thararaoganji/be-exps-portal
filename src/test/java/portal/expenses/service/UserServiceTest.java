package portal.expenses.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
        user.setUsername("testuser");
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRoles(Set.of(role));

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).username());
        assertEquals("Test User", result.get(0).name());
        assertEquals("test@example.com", result.get(0).email());
        assertTrue(result.get(0).roles().contains("ROLE_USER"));
    }

    @Test
    void testCreateUser_Success() {
        UserCreateRequest request = new UserCreateRequest("newuser", "password123", "New User", "new@example.com", "ROLE_EMPLOYEE");

        Role role = new Role();
        role.setName("ROLE_EMPLOYEE");

        AppUser savedUser = new AppUser();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setPassword("encodedPassword");
        savedUser.setName("New User");
        savedUser.setEmail("new@example.com");
        savedUser.setRoles(Set.of(role));

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_EMPLOYEE")).thenReturn(Optional.of(role));
        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        UserResponseDto response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("newuser", response.username());
        assertEquals("New User", response.name());
        assertEquals("new@example.com", response.email());
        assertTrue(response.roles().contains("ROLE_EMPLOYEE"));
    }

    @Test
    void testCreateUser_RoleNotFound() {
        UserCreateRequest request = new UserCreateRequest("newuser", "password123", "New User", "new@example.com", "ROLE_INVALID");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_INVALID")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(request));
        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(5L);
        verify(userRepository).deleteById(5L);
    }

    @Test
    void testGetAllUsersWithPagination_Ascending() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        AppUser user = new AppUser();
        user.setId(10L);
        user.setUsername("adminuser");
        user.setName("Admin User");
        user.setEmail("admin@example.com");
        user.setRoles(Set.of(role));

        Page<AppUser> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 10, "username", "ASC");

        assertNotNull(response);
    }

    @Test
    void testGetAllUsersWithPagination_Descending() {
        Role role = new Role();
        role.setName("ROLE_MANAGER");

        AppUser user = new AppUser();
        user.setId(20L);
        user.setUsername("manageruser");
        user.setName("Manager User");
        user.setEmail("manager@example.com");
        user.setRoles(Set.of(role));

        Page<AppUser> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 5, "id", "DESC");

        assertNotNull(response);
    }
}