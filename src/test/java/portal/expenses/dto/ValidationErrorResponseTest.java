package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ValidationErrorResponseTest {

    @Test
    void testConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        int status = 400;
        String error = "Bad Request";
        String message = "Validation failed";
        String path = "/api/expenses";
        Map<String, String> fieldErrors = Collections.singletonMap("amount", "Amount must be positive");

        ValidationErrorResponse response = new ValidationErrorResponse(now, status, error, message, path, fieldErrors);

        assertEquals(now, response.getTimestamp());
        assertEquals(status, response.getStatus());
        assertEquals(error, response.getError());
        assertEquals(message, response.getMessage());
        assertEquals(path, response.getPath());
        assertNotNull(response.getFieldErrors());
        assertEquals(fieldErrors, response.getFieldErrors());
        assertEquals("Amount must be positive", response.getFieldErrors().get("amount"));
    }
}