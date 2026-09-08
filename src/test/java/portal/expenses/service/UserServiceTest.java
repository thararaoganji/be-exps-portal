package portal.expenses.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @InjectMocks
    private UserService userService;

    private AppUser sampleUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = new Role();
        sampleRole.setName("ROLE_USER");

        sampleUser = new AppUser();
        sampleUser.setId(1L);
        sampleUser.setUsername("johndoe");
        sampleUser.setPassword("encodedPassword");
        sampleUser.setName("John Doe");
        sampleUser.setEmail("john@example.com");
        sampleUser.setRoles(Set.of(sampleRole));
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<UserResponseDto> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("johndoe", users.get(0).username());
        assertEquals("John Doe", users.get(0).name());
        assertEquals("john@example.com", users.get(0).email());
        assertTrue(users.get(0).roles().contains("ROLE_USER"));
    }

    @Test
    void testCreateUser_Success() {
        UserCreateRequest request = new UserCreateRequest("newuser", "rawPassword", "New User", "new@example.com", "ROLE_USER");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(sampleRole));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        UserResponseDto response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("newuser", response.username());
        assertEquals("New User", response.name());
        assertEquals("new@example.com", response.email());
        assertTrue(response.roles().contains("ROLE_USER"));

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        AppUser captured = userCaptor.getValue();
        assertEquals("newuser", captured.getUsername());
        assertEquals("encodedPassword", captured.getPassword());
        assertEquals("New User", captured.getName());
        assertEquals("new@example.com", captured.getEmail());
        assertEquals(Set.of(sampleRole), captured.getRoles());
    }

    @Test
    void testCreateUser_RoleNotFound() {
        UserCreateRequest request = new UserCreateRequest("newuser", "rawPassword", "New User", "new@example.com", "ROLE_UNKNOWN");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
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
    void testGetAllUsersWithPagination_Ascending() {
        Page<AppUser> page = new PageImpl<>(List.of(sampleUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(0, 10, "username", "ASC");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPageNumber());
        assertEquals(1, response.getPageSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());
        assertEquals(Sort.Direction.ASC, capturedPageable.getSort().getOrderFor("username").getDirection());
    }

    @Test
    void testGetAllUsersWithPagination_Descending() {
        Page<AppUser> page = new PageImpl<>(List.of(sampleUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsersWithPagination(1, 5, "id", "DESC");

        assertNotNull(response);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(1, capturedPageable.getPageNumber());
        assertEquals(5, capturedPageable.getPageSize());
        assertEquals(Sort.Direction.DESC, capturedPageable.getSort().getOrderFor("id").getDirection());
    }
}