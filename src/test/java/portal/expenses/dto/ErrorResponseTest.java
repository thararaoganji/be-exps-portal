package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorResponseTest {

    @Test
    void testErrorResponseConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        int status = 404;
        String error = "Not Found";
        String message = "Resource not found";
        String path = "/api/v1/resource";

        ErrorResponse errorResponse = new ErrorResponse(now, status, error, message, path);

        assertNotNull(errorResponse);
        assertEquals(now, errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
    }
}