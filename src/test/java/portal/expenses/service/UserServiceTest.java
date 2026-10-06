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

    private AppUser buildAppUser(Long id, String username, String name, String email, String roleName) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setName(name);
        user.setEmail(email);
        Role role = new Role();
        role.setName(roleName);
        user.setRoles(Set.of(role));
        return user;
    }

    @Test
    void getAllUsers_returnsListOfUserResponseDto() {
        AppUser user = buildAppUser(1L, "john", "John Doe", "john@example.com", "ROLE_USER");
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
    void createUser_successfulCreation() {
        UserCreateRequest request = new UserCreateRequest("john", "rawPass", "John Doe", "john@example.com", "ROLE_USER");
        Role role = new Role();
        role.setName("ROLE_USER");

        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        AppUser savedUser = buildAppUser(1L, "john", "John Doe", "john@example.com", "ROLE_USER");
        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        UserResponseDto response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("john", response.username());
        assertEquals("John Doe", response.name());
        assertEquals("john@example.com", response.email());
        assertTrue(response.roles().contains("ROLE_USER"));
    }

    @Test
    void createUser_throwsExceptionWhenRoleNotFound() {
        UserCreateRequest request = new UserCreateRequest("john", "rawPass", "John Doe", "john@example.com", "ROLE_UNKNOWN");
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(request));
        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    void deleteUser_callsRepositoryDeleteById() {
        userService.deleteUser(5L);
        verify(userRepository).deleteById(5L);
    }

    @Test
    void getAllUsersWithPagination_descSortDirection() {
        AppUser user = buildAppUser(1L, "john", "John Doe", "john@example.com", "ROLE_USER");
        Page<AppUser> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 10, "username", "DESC");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("john", response.getContent().get(0).username());
    }

    @Test
    void getAllUsersWithPagination_ascSortDirection() {
        AppUser user = buildAppUser(2L, "jane", "Jane Doe", "jane@example.com", "ROLE_ADMIN");
        Page<AppUser> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 10, "username", "ASC");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("jane", response.getContent().get(0).username());
    }
}