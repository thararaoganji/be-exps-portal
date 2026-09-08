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
        user.setUsername("johndoe");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setRoles(Set.of(role));

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("johndoe", result.get(0).username());
        assertEquals("John Doe", result.get(0).name());
        assertEquals("john@example.com", result.get(0).email());
        assertTrue(result.get(0).roles().contains("ROLE_USER"));
    }

    @Test
    void testCreateUser_Success() {
        UserCreateRequest request = new UserCreateRequest("johndoe", "rawPassword", "John Doe", "john@example.com", "ROLE_USER");

        Role role = new Role();
        role.setName("ROLE_USER");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        AppUser savedUser = new AppUser();
        savedUser.setId(10L);
        savedUser.setUsername("johndoe");
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setRoles(Set.of(role));

        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        UserResponseDto response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("johndoe", response.username());
        assertEquals("John Doe", response.name());
        assertEquals("john@example.com", response.email());
        assertTrue(response.roles().contains("ROLE_USER"));
    }

    @Test
    void testCreateUser_RoleNotFound() {
        UserCreateRequest request = new UserCreateRequest("johndoe", "rawPassword", "John Doe", "john@example.com", "NON_EXISTENT_ROLE");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(roleRepository.findByName("NON_EXISTENT_ROLE")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(request));
        assertEquals("Role not found", exception.getMessage());
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
        role.setName("ROLE_ADMIN");

        AppUser user = new AppUser();
        user.setId(2L);
        user.setUsername("admin");
        user.setName("Admin User");
        user.setEmail("admin@example.com");
        user.setRoles(Set.of(role));

        Page<AppUser> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 10, "username", "ASC");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("admin", response.getContent().get(0).username());
        assertEquals(0, response.getPageNumber());
        assertEquals(1, response.getTotalElements());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    void testGetAllUsersWithPagination_Descending() {
        Role role = new Role();
        role.setName("ROLE_MANAGER");

        AppUser user = new AppUser();
        user.setId(3L);
        user.setUsername("manager");
        user.setName("Manager User");
        user.setEmail("manager@example.com");
        user.setRoles(Set.of(role));

        Page<AppUser> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 5, "id", "DESC");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("manager", response.getContent().get(0).username());
    }
}