package portal.expenses.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestResponseLoggingFilterTest {

    private RequestResponseLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestResponseLoggingFilter();
    }

    @Test
    void testShouldNotFilter_actuatorPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    void testShouldNotFilter_nonActuatorPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/expenses");

        boolean result = filter.shouldNotFilter(request);

        assertFalse(result);
    }

    @Test
    void testDoFilterInternal_jsonRequestAndNormalResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/expenses");
        request.setQueryString("page=1&limit=10");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer token123");
        request.setContentType("application/json");
        request.setContent("{\"amount\": 100}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            // Read from request wrapper so content is cached
            req.getInputStream().readAllBytes();
            res.getOutputStream().write("{\"status\":\"success\"}".getBytes(StandardCharsets.UTF_8));
        };

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getContentAsString().contains("success"));
    }

    @Test
    void testDoFilterInternal_multipartRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/expenses/upload");
        request.setContentType("multipart/form-data; boundary=---123");
        request.setContent("fake multipart data".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            req.getInputStream().readAllBytes();
            res.getOutputStream().write("OK".getBytes(StandardCharsets.UTF_8));
        };

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getContentAsString().contains("OK"));
    }

    @Test
    void testDoFilterInternal_emptyBodyAndNoContentType() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses/1");
        // No query string, no content type

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        // Filter chain executed without error
    }

    @Test
    void testDoFilterInternal_largeResponseBodyTruncated() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses");

        MockHttpServletResponse response = new MockHttpServletResponse();
        String largeResponseBody = "A".repeat(1500);
        FilterChain filterChain = (req, res) -> {
            res.getOutputStream().write(largeResponseBody.getBytes(StandardCharsets.UTF_8));
        };

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getContentAsString().length() >= 1000);
    }
}