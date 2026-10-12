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
import org.mockito.ArgumentCaptor;
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

    private AppUser buildUser(Long id, String username, String name, String email, String roleName) {
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
    void testGetAllUsers() {
        AppUser user = buildUser(1L, "john", "John Doe", "john@example.com", "ROLE_USER");
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDto> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        UserResponseDto dto = users.get(0);
        assertEquals(1L, dto.id());
        assertEquals("john", dto.username());
        assertEquals("John Doe", dto.name());
        assertEquals("john@example.com", dto.email());
        assertTrue(dto.roles().contains("ROLE_USER"));
    }

    @Test
    void testCreateUser_Success() {
        UserCreateRequest request = new UserCreateRequest("john", "plainPassword", "John Doe", "john@example.com", "ROLE_USER");
        Role role = new Role();
        role.setName("ROLE_USER");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        AppUser savedUser = buildUser(10L, "john", "John Doe", "john@example.com", "ROLE_USER");
        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        UserResponseDto response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("john", response.username());
        assertEquals("John Doe", response.name());
        assertEquals("john@example.com", response.email());
        assertTrue(response.roles().contains("ROLE_USER"));

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        AppUser captured = userCaptor.getValue();
        assertEquals("john", captured.getUsername());
        assertEquals("encodedPassword", captured.getPassword());
        assertEquals("John Doe", captured.getName());
        assertEquals("john@example.com", captured.getEmail());
    }

    @Test
    void testCreateUser_RoleNotFound() {
        UserCreateRequest request = new UserCreateRequest("john", "plainPassword", "John Doe", "john@example.com", "NON_EXISTENT");
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(roleRepository.findByName("NON_EXISTENT")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(request));
        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(123L);
        verify(userRepository).deleteById(123L);
    }

    @Test
    void testGetAllUsersWithPagination_Ascending() {
        AppUser user = buildUser(1L, "jane", "Jane Doe", "jane@example.com", "ROLE_ADMIN");
        Page<AppUser> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 10, "username", "ASC");

        assertNotNull(response);
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void testGetAllUsersWithPagination_Descending() {
        AppUser user = buildUser(2L, "alex", "Alex Doe", "alex@example.com", "ROLE_USER");
        Page<AppUser> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 5, "id", "DESC");

        assertNotNull(response);
        verify(userRepository).findAll(any(Pageable.class));
    }
}