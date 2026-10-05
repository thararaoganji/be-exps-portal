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
    void testShouldNotFilter() {
        MockHttpServletRequest actuatorRequest = new MockHttpServletRequest();
        actuatorRequest.setRequestURI("/actuator/health");
        assertTrue(filter.shouldNotFilter(actuatorRequest));

        MockHttpServletRequest apiRequest = new MockHttpServletRequest();
        apiRequest.setRequestURI("/api/expenses");
        assertFalse(filter.shouldNotFilter(apiRequest));
    }

    @Test
    void testDoFilterInternal_WithJsonBodyAndQueryString() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/expenses");
        request.setQueryString("page=1&size=10");
        request.addHeader("Authorization", "Bearer token123");
        request.addHeader("Content-Type", "application/json");
        request.setContentType("application/json");
        request.setContent("{\"amount\": 100.50}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            res.getOutputStream().write("{\"status\": \"success\"}".getBytes(StandardCharsets.UTF_8));
            ((HttpServletResponse) res).setStatus(200);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getContentAsString().contains("success"));
        assertEquals(200, response.getStatus());
    }

    @Test
    void testDoFilterInternal_WithMultipartFormData() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/upload");
        request.setContentType("multipart/form-data");
        request.addHeader("Content-Type", "multipart/form-data");

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(201);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(201, response.getStatus());
    }

    @Test
    void testDoFilterInternal_LongResponseBodyTruncation() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String longBody = "A".repeat(1500);

        FilterChain filterChain = (req, res) -> {
            res.getOutputStream().write(longBody.getBytes(StandardCharsets.UTF_8));
            ((HttpServletResponse) res).setStatus(200);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(1500, response.getContentAsString().length());
    }

    @Test
    void testDoFilterInternal_EmptyBodiesAndNullContentType() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(204);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(204, response.getStatus());
    }
}