package portal.expenses.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestResponseLoggingFilterTest {

    private RequestResponseLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestResponseLoggingFilter();
    }

    @Test
    void shouldNotFilter_whenActuatorPath_shouldReturnTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    void shouldNotFilter_whenNonActuatorPath_shouldReturnFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/expenses");

        boolean result = filter.shouldNotFilter(request);

        assertFalse(result);
    }

    @Test
    void doFilterInternal_withJsonBodyAndShortResponse_shouldLogAndCopyBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/v1/expenses");
        request.setQueryString("sort=desc");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer token123");
        request.setContentType("application/json");
        request.setContent("{\"amount\": 100.50}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(200);
            res.getOutputStream().write("{\"success\": true}".getBytes(StandardCharsets.UTF_8));
        };

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getContentAsString().contains("{\"success\": true}"));
    }

    @Test
    void doFilterInternal_withMultipartContentType_shouldLogMultipartNotice() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/v1/expenses/receipt");
        request.setContentType("multipart/form-data; boundary=---1234");
        request.setContent("dummy binary data".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(201);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(201, response.getStatus());
    }

    @Test
    void doFilterInternal_withLongResponseBody_shouldTruncateInLog() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/v1/expenses");

        MockHttpServletResponse response = new MockHttpServletResponse();

        String longBody = "A".repeat(1200);

        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(200);
            res.getOutputStream().write(longBody.getBytes(StandardCharsets.UTF_8));
        };

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getContentAsString().length() >= 1200);
    }

    @Test
    void doFilterInternal_withEmptyRequestBodyAndResponse_shouldExecuteSuccessfully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/v1/expenses");
        request.setQueryString(null);

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(204);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(204, response.getStatus());
    }
}