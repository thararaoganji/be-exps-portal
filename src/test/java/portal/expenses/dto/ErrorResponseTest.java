package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErrorResponseTest {

    @Test
    void testConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        int status = 404;
        String error = "Not Found";
        String message = "Resource not found";
        String path = "/api/v1/resource";

        ErrorResponse response = new ErrorResponse(now, status, error, message, path);

        assertEquals(now, response.getTimestamp());
        assertEquals(status, response.getStatus());
        assertEquals(error, response.getError());
        assertEquals(message, response.getMessage());
        assertEquals(path, response.getPath());
    }

    @Test
    void testConstructorWithNullValues() {
        ErrorResponse response = new ErrorResponse(null, 0, null, null, null);

        assertNull(response.getTimestamp());
        assertEquals(0, response.getStatus());
        assertNull(response.getError());
        assertNull(response.getMessage());
        assertNull(response.getPath());
    }
}