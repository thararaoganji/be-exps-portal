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
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 400;
        String error = "Bad Request";
        String message = "Validation failed";
        String path = "/api/v1/expenses";
        Map<String, String> fieldErrors = Collections.singletonMap("amount", "Amount is required");

        ValidationErrorResponse response = new ValidationErrorResponse(
                timestamp,
                status,
                error,
                message,
                path,
                fieldErrors
        );

        assertNotNull(response);
        assertEquals(fieldErrors, response.getFieldErrors());
        assertEquals("Amount is required", response.getFieldErrors().get("amount"));
    }
}