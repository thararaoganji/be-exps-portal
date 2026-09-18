package portal.expenses.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void getAllUsers_returnsListOfUserResponseDto() {
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
        verify(userRepository).findAll();
    }

    @Test
    void createUser_success() {
        UserCreateRequest request = new UserCreateRequest("john", "password123", "John Doe", "john@example.com", "ROLE_USER");

        Role role = new Role();
        role.setName("ROLE_USER");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        AppUser savedUser = new AppUser();
        savedUser.setId(1L);
        savedUser.setUsername("john");
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setRoles(Set.of(role));

        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        UserResponseDto result = userService.createUser(request);

        assertNotNull(result);
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void createUser_roleNotFound_throwsRuntimeException() {
        UserCreateRequest request = new UserCreateRequest("john", "password123", "John Doe", "john@example.com", "ROLE_UNKNOWN");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(request));
        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    void deleteUser_success() {
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void getAllUsersWithPagination_descendingSort() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        AppUser user = new AppUser();
        user.setId(2L);
        user.setUsername("jane");
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setRoles(Set.of(role));

        Page<AppUser> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 10, "username", "DESC");

        assertNotNull(response);
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllUsersWithPagination_ascendingSort() {
        Page<AppUser> page = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 10, "username", "ASC");

        assertNotNull(response);
        verify(userRepository).findAll(any(Pageable.class));
    }
}