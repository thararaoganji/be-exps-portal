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
    void getAllUsers_returnsListOfUsers() {
        UserResponseDto mockDto = mock(UserResponseDto.class);
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(mockDto));

        ResponseEntity<List<UserResponseDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getAllUsersPaginated_returnsPageResponse() {
        @SuppressWarnings("unchecked")
        PageResponse<UserResponseDto> mockPageResponse = mock(PageResponse.class);
        when(userService.getAllUsersWithPagination(0, 10, "username", "ASC")).thenReturn(mockPageResponse);

        ResponseEntity<PageResponse<UserResponseDto>> response =
                userController.getAllUsersPaginated(0, 10, "username", "ASC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPageResponse, response.getBody());
        verify(userService, times(1)).getAllUsersWithPagination(0, 10, "username", "ASC");
    }

    @Test
    void createUser_returnsCreatedUser() {
        UserCreateRequest request = mock(UserCreateRequest.class);
        UserResponseDto mockDto = mock(UserResponseDto.class);
        when(userService.createUser(request)).thenReturn(mockDto);

        ResponseEntity<UserResponseDto> response = userController.createUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDto, response.getBody());
        verify(userService, times(1)).createUser(request);
    }

    @Test
    void deleteUser_returnsOk() {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        ResponseEntity<Void> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).deleteUser(userId);
    }
}