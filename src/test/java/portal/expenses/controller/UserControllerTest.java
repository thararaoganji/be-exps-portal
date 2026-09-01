package portal.expenses.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import portal.expenses.dto.PageResponse;
import portal.expenses.dto.UserCreateRequest;
import portal.expenses.dto.UserResponseDto;
import portal.expenses.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
    }

    @Test
    void testGetAllUsers() {
        UserResponseDto mockUser = mock(UserResponseDto.class);
        List<UserResponseDto> userList = Collections.singletonList(mockUser);
        when(userService.getAllUsers()).thenReturn(userList);

        ResponseEntity<List<UserResponseDto>> response = userController.getAllUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userList, response.getBody());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetAllUsersPaginated() {
        PageResponse<UserResponseDto> mockPageResponse = mock(PageResponse.class);
        when(userService.getAllUsersWithPagination(0, 10, "username", "ASC"))
                .thenReturn(mockPageResponse);

        ResponseEntity<PageResponse<UserResponseDto>> response =
                userController.getAllUsersPaginated(0, 10, "username", "ASC");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPageResponse, response.getBody());
        verify(userService, times(1)).getAllUsersWithPagination(0, 10, "username", "ASC");
    }

    @Test
    void testCreateUser() {
        UserCreateRequest request = mock(UserCreateRequest.class);
        UserResponseDto mockResponse = mock(UserResponseDto.class);
        when(userService.createUser(request)).thenReturn(mockResponse);

        ResponseEntity<UserResponseDto> response = userController.createUser(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(userService, times(1)).createUser(request);
    }

    @Test
    void testDeleteUser() {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        ResponseEntity<Void> response = userController.deleteUser(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).deleteUser(userId);
    }
}