package portal.expenses.controller;

import org.junit.jupiter.api.BeforeEach;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetAllUsers() {
        UserResponseDto userDto = mock(UserResponseDto.class);
        List<UserResponseDto> list = List.of(userDto);
        when(userService.getAllUsers()).thenReturn(list);

        ResponseEntity<List<UserResponseDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());
        verify(userService).getAllUsers();
    }

    @Test
    void testGetAllUsersPaginated() {
        @SuppressWarnings("unchecked")
        PageResponse<UserResponseDto> pageResponse = mock(PageResponse.class);
        when(userService.getAllUsersWithPagination(0, 10, "id", "ASC")).thenReturn(pageResponse);

        ResponseEntity<PageResponse<UserResponseDto>> response = userController.getAllUsersPaginated(0, 10, "id", "ASC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
        verify(userService).getAllUsersWithPagination(0, 10, "id", "ASC");
    }

    @Test
    void testCreateUser() {
        UserCreateRequest request = mock(UserCreateRequest.class);
        UserResponseDto created = mock(UserResponseDto.class);
        when(userService.createUser(request)).thenReturn(created);

        ResponseEntity<UserResponseDto> response = userController.createUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(created, response.getBody());
        verify(userService).createUser(request);
    }

    @Test
    void testDeleteUser() {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        ResponseEntity<Void> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deleteUser(userId);
    }
}