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
        LocalDateTime now = LocalDateTime.now();
        int status = 400;
        String error = "Bad Request";
        String message = "Validation failed";
        String path = "/api/expenses";
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("amount", "Amount must be positive");
        fieldErrors.put("description", "Description is required");

        ValidationErrorResponse response = new ValidationErrorResponse(
                now,
                status,
                error,
                message,
                path,
                fieldErrors
        );

        assertEquals(now, response.getTimestamp());
        assertEquals(status, response.getStatus());
        assertEquals(error, response.getError());
        assertEquals(message, response.getMessage());
        assertEquals(path, response.getPath());
        assertNotNull(response.getFieldErrors());
        assertEquals(2, response.getFieldErrors().size());
        assertEquals("Amount must be positive", response.getFieldErrors().get("amount"));
        assertEquals("Description is required", response.getFieldErrors().get("description"));
    }

    @Test
    void testConstructorWithNullOrEmptyFieldErrors() {
        LocalDateTime now = LocalDateTime.now();
        ValidationErrorResponse responseWithNull = new ValidationErrorResponse(
                now, 422, "Unprocessable Entity", "Validation error", "/api/test", null
        );
        assertNull(responseWithNull.getFieldErrors());

        ValidationErrorResponse responseWithEmpty = new ValidationErrorResponse(
                now, 422, "Unprocessable Entity", "Validation error", "/api/test", Collections.emptyMap()
        );
        assertNotNull(responseWithEmpty.getFieldErrors());
        assertEquals(0, responseWithEmpty.getFieldErrors().size());
    }
}