package portal.expenses.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import portal.expenses.dto.ErrorResponse;
import portal.expenses.dto.ValidationErrorResponse;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleNotFoundExceptionWithUsernameNotFoundException() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users/123");
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFoundException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("User not found", response.getBody().getMessage());
        assertEquals("/api/users/123", response.getBody().getPath());
    }

    @Test
    void testHandleNotFoundExceptionWithNoSuchElementException() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/expenses/456");
        NoSuchElementException ex = new NoSuchElementException("Expense not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFoundException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Expense not found", response.getBody().getMessage());
        assertEquals("/api/expenses/456", response.getBody().getPath());
    }

    @Test
    void testHandleAuthenticationException() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/auth/login");
        AuthenticationException ex = new BadCredentialsException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Invalid credentials", response.getBody().getMessage());
        assertEquals("/api/auth/login", response.getBody().getPath());
    }

    @Test
    void testHandleBadRequestException() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/expenses");
        IllegalArgumentException ex = new IllegalArgumentException("Invalid amount");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequestException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Invalid amount", response.getBody().getMessage());
        assertEquals("/api/expenses", response.getBody().getPath());
    }

    @Test
    void testHandleGenericException() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/error");
        Exception ex = new RuntimeException("Unexpected database failure");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Unexpected database failure", response.getBody().getMessage());
        assertEquals("/api/error", response.getBody().getPath());
    }

    @Test
    void testHandleValidationExceptions() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/expenses/create");
        FieldError fieldError1 = new FieldError("expenseRequest", "amount", "Amount must be positive");
        FieldError fieldError2 = new FieldError("expenseRequest", "category", "Category is required");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ValidationErrorResponse> response = exceptionHandler.handleValidationExceptions(methodArgumentNotValidException, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("/api/expenses/create", response.getBody().getPath());
    }
}