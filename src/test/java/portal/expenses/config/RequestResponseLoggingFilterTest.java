package portal.expenses.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
        MockHttpServletRequest actuatorRequest = new MockHttpServletRequest("GET", "/actuator/health");
        assertTrue(filter.shouldNotFilter(actuatorRequest));

        MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/api/expenses");
        assertFalse(filter.shouldNotFilter(apiRequest));
    }

    @Test
    void testDoFilterInternal_jsonRequestAndResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/expenses");
        request.setQueryString("debug=true");
        request.addHeader("Authorization", "Bearer token123");
        request.setContentType("application/json");
        request.setContent("{\"amount\": 100}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            // Read request content from wrapped request
            req.getInputStream().readAllBytes();
            res.getOutputStream().write("{\"status\": \"success\"}".getBytes(StandardCharsets.UTF_8));
            ((jakarta.servlet.http.HttpServletResponse) res).setStatus(200);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getContentAsString().contains("{\"status\": \"success\"}"));
    }

    @Test
    void testDoFilterInternal_multipartRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/upload");
        request.setContentType("multipart/form-data; boundary=----WebKitFormBoundary");
        request.setContent("file-data".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            ((jakarta.servlet.http.HttpServletResponse) res).setStatus(200);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void testDoFilterInternal_largeResponseBodyTruncation() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/large");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String longBody = "A".repeat(1500);

        FilterChain filterChain = (req, res) -> {
            res.getOutputStream().write(longBody.getBytes(StandardCharsets.UTF_8));
            ((jakarta.servlet.http.HttpServletResponse) res).setStatus(200);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getContentAsString().length() >= 1500);
    }

    @Test
    void testDoFilterInternal_noContentTypeAndEmptyBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/empty");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            ((jakarta.servlet.http.HttpServletResponse) res).setStatus(204);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(204, response.getStatus());
    }
}