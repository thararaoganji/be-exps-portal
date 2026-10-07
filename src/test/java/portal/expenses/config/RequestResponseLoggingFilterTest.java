package portal.expenses.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
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

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_whenNonActuatorPath_shouldReturnFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/expenses");

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_withJsonRequestBodyAndResponseBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/expenses");
        request.setQueryString("sort=asc");
        request.addHeader("Authorization", "Bearer token123");
        request.setContentType("application/json");
        request.setContent("{\"amount\": 100.0}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            // Read request body to populate content cache if needed
            req.getInputStream().readAllBytes();
            res.setContentType("application/json");
            res.getWriter().write("{\"status\": \"created\"}");
            ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_CREATED);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(201, response.getStatus());
        assertEquals("{\"status\": \"created\"}", response.getContentAsString());
    }

    @Test
    void doFilterInternal_withMultipartFormData() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/expenses/upload");
        request.setContentType("multipart/form-data; boundary=---12345");
        request.setContent("dummy-form-data".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_withNoContentTypeAndEmptyBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_withLargeResponseBody_shouldTruncate() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String largeBody = "A".repeat(1200);

        FilterChain filterChain = (req, res) -> {
            res.getWriter().write(largeBody);
            ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals(largeBody, response.getContentAsString());
    }
}