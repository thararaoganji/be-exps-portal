package portal.expenses.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import portal.expenses.dto.PageResponse;
import portal.expenses.dto.UserCreateRequest;
import portal.expenses.dto.UserResponseDto;
import portal.expenses.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetAllUsers() {
        UserResponseDto mockUser = mock(UserResponseDto.class);
        when(userService.getAllUsers()).thenReturn(List.of(mockUser));

        ResponseEntity<List<UserResponseDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(userService).getAllUsers();
    }

    @Test
    void testGetAllUsersPaginated() {
        PageResponse<UserResponseDto> mockPageResponse = new PageResponse<>();
        when(userService.getAllUsersWithPagination(0, 10, "username", "ASC"))
                .thenReturn(mockPageResponse);

        ResponseEntity<PageResponse<UserResponseDto>> response =
                userController.getAllUsersPaginated(0, 10, "username", "ASC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPageResponse, response.getBody());
        verify(userService).getAllUsersWithPagination(0, 10, "username", "ASC");
    }

    @Test
    void testCreateUser() {
        UserCreateRequest request = mock(UserCreateRequest.class);
        UserResponseDto mockResponse = mock(UserResponseDto.class);
        when(userService.createUser(request)).thenReturn(mockResponse);

        ResponseEntity<UserResponseDto> response = userController.createUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(userService).createUser(request);
    }

    @Test
    void testDeleteUser() {
        Long userId = 1L;

        ResponseEntity<Void> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deleteUser(userId);
    }
}