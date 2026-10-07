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
        String path = "/api/v1/test";
        Map<String, String> fieldErrors = Collections.singletonMap("field", "must not be null");

        ValidationErrorResponse response = new ValidationErrorResponse(timestamp, status, error, message, path, fieldErrors);

        assertEquals(timestamp, response.getTimestamp());
        assertEquals(status, response.getStatus());
        assertEquals(error, response.getError());
        assertEquals(message, response.getMessage());
        assertEquals(path, response.getPath());
        assertNotNull(response.getFieldErrors());
        assertEquals("must not be null", response.getFieldErrors().get("field"));
    }
}