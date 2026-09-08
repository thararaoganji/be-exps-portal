package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ValidationErrorResponseTest {

    @Test
    void testConstructorAndGetters() {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 400;
        String error = "Bad Request";
        String message = "Validation failed";
        String path = "/api/expenses";
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("amount", "Amount must be greater than 0");
        fieldErrors.put("title", "Title cannot be blank");

        ValidationErrorResponse response = new ValidationErrorResponse(
                timestamp, status, error, message, path, fieldErrors
        );

        assertEquals(timestamp, response.getTimestamp());
        assertEquals(status, response.getStatus());
        assertEquals(error, response.getError());
        assertEquals(message, response.getMessage());
        assertEquals(path, response.getPath());
        assertNotNull(response.getFieldErrors());
        assertEquals(2, response.getFieldErrors().size());
        assertEquals("Amount must be greater than 0", response.getFieldErrors().get("amount"));
        assertEquals("Title cannot be blank", response.getFieldErrors().get("title"));
    }

    @Test
    void testConstructorWithEmptyFieldErrors() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        ValidationErrorResponse response = new ValidationErrorResponse(
                timestamp, 422, "Unprocessable Entity", "Invalid payload", "/api/test", Collections.emptyMap()
        );

        assertEquals(timestamp, response.getTimestamp());
        assertEquals(422, response.getStatus());
        assertEquals("Unprocessable Entity", response.getError());
        assertEquals("Invalid payload", response.getMessage());
        assertEquals("/api/test", response.getPath());
        assertNotNull(response.getFieldErrors());
        assertEquals(0, response.getFieldErrors().size());
    }

    @Test
    void testConstructorWithNullFieldErrors() {
        LocalDateTime timestamp = LocalDateTime.now();
        ValidationErrorResponse response = new ValidationErrorResponse(
                timestamp, 400, "Error", "Validation Error", "/path", null
        );

        assertEquals(timestamp, response.getTimestamp());
        assertEquals(400, response.getStatus());
        assertEquals("Error", response.getError());
        assertEquals("Validation Error", response.getMessage());
        assertEquals("/path", response.getPath());
        assertNull(response.getFieldErrors());
    }
}