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
        errors.put("category", "Category is required");

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
        assertEquals("Category is required", response.getFieldErrors().get("category"));
    }

    @Test
    void testConstructorWithNullOrEmptyFieldErrors() {
        LocalDateTime now = LocalDateTime.now();
        ValidationErrorResponse responseWithNull = new ValidationErrorResponse(
                now,
                422,
                "Unprocessable Entity",
                "Validation errors",
                "/api/test",
                null
        );

        assertNull(responseWithNull.getFieldErrors());

        ValidationErrorResponse responseWithEmpty = new ValidationErrorResponse(
                now,
                400,
                "Bad Request",
                "No specific field errors",
                "/api/test",
                Collections.emptyMap()
        );

        assertNotNull(responseWithEmpty.getFieldErrors());
        assertEquals(0, responseWithEmpty.getFieldErrors().size());
    }
}