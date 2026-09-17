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
        UserResponseDto userDto = mock(UserResponseDto.class);
        when(userService.getAllUsers()).thenReturn(List.of(userDto));

        ResponseEntity<List<UserResponseDto>> response = userController.getAllUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetAllUsersPaginated() {
        @SuppressWarnings("unchecked")
        PageResponse<UserResponseDto> pageResponse = mock(PageResponse.class);
        when(userService.getAllUsersWithPagination(0, 10, "username", "ASC")).thenReturn(pageResponse);

        ResponseEntity<PageResponse<UserResponseDto>> response = userController.getAllUsersPaginated(0, 10, "username", "ASC");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
        verify(userService, times(1)).getAllUsersWithPagination(0, 10, "username", "ASC");
    }

    @Test
    void testCreateUser() {
        UserCreateRequest request = mock(UserCreateRequest.class);
        UserResponseDto responseDto = mock(UserResponseDto.class);
        when(userService.createUser(request)).thenReturn(responseDto);

        ResponseEntity<UserResponseDto> response = userController.createUser(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
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