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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        UserResponseDto dto = mock(UserResponseDto.class);
        List<UserResponseDto> users = Collections.singletonList(dto);
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserResponseDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(userService).getAllUsers();
    }

    @Test
    void testGetAllUsersPaginated() {
        @SuppressWarnings("unchecked")
        PageResponse<UserResponseDto> pageResponse = mock(PageResponse.class);
        when(userService.getAllUsersWithPagination(0, 10, "id", "ASC")).thenReturn(pageResponse);

        ResponseEntity<PageResponse<UserResponseDto>> response =
                userController.getAllUsersPaginated(0, 10, "id", "ASC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
        verify(userService).getAllUsersWithPagination(0, 10, "id", "ASC");
    }

    @Test
    void testCreateUser() {
        UserCreateRequest request = mock(UserCreateRequest.class);
        UserResponseDto dto = mock(UserResponseDto.class);
        when(userService.createUser(request)).thenReturn(dto);

        ResponseEntity<UserResponseDto> response = userController.createUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
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