package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ValidationErrorResponseTest {

    @Test
    void testValidationErrorResponse() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> fieldErrors = Collections.singletonMap("amount", "Amount must be positive");

        ValidationErrorResponse response = new ValidationErrorResponse(
                now,
                400,
                "Bad Request",
                "Validation failed",
                "/api/expenses",
                fieldErrors
        );

        assertNotNull(response);
        assertEquals(fieldErrors, response.getFieldErrors());
        assertEquals(1, response.getFieldErrors().size());
        assertEquals("Amount must be positive", response.getFieldErrors().get("amount"));
    }
}