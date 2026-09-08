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
        Map<String, String> errors = new HashMap<>();
        errors.put("amount", "Amount must be positive");
        errors.put("title", "Title is required");

        ValidationErrorResponse response = new ValidationErrorResponse(
                now,
                400,
                "Bad Request",
                "Validation failed",
                "/api/expenses",
                errors
        );

        assertEquals(now, response.getTimestamp());
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Validation failed", response.getMessage());
        assertEquals("/api/expenses", response.getPath());
        assertNotNull(response.getFieldErrors());
        assertEquals(2, response.getFieldErrors().size());
        assertEquals("Amount must be positive", response.getFieldErrors().get("amount"));
        assertEquals("Title is required", response.getFieldErrors().get("title"));
    }

    @Test
    void testConstructorWithNullOrEmptyFieldErrors() {
        LocalDateTime now = LocalDateTime.now();

        ValidationErrorResponse responseNullErrors = new ValidationErrorResponse(
                now,
                400,
                "Bad Request",
                "Validation failed",
                "/api/test",
                null
        );
        assertNull(responseNullErrors.getFieldErrors());

        ValidationErrorResponse responseEmptyErrors = new ValidationErrorResponse(
                now,
                422,
                "Unprocessable Entity",
                "Validation failed",
                "/api/test",
                Collections.emptyMap()
        );
        assertNotNull(responseEmptyErrors.getFieldErrors());
        assertEquals(0, responseEmptyErrors.getFieldErrors().size());
    }
}