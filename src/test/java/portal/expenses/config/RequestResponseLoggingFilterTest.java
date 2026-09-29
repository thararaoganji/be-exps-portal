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
    void shouldNotFilter_actuatorPath_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_nonActuatorPath_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/expenses");

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_withJsonRequestBodyAndResponse_logsAndCopiesBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/expenses");
        request.setMethod("POST");
        request.setQueryString("sort=desc");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer token");
        request.setContentType("application/json");
        request.setContent("{\"amount\": 100}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            // Read request body to cache it
            req.getInputStream().readAllBytes();
            res.getOutputStream().write("{\"status\": \"OK\"}".getBytes(StandardCharsets.UTF_8));
            if (res instanceof HttpServletResponse httpServletResponse) {
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            }
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("{\"status\": \"OK\"}", response.getContentAsString());
    }

    @Test
    void doFilterInternal_withMultipartRequest_logsMultipartPlaceholder() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/expenses/upload");
        request.setMethod("POST");
        request.setContentType("multipart/form-data; boundary=---123");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            if (res instanceof HttpServletResponse httpServletResponse) {
                httpServletResponse.setStatus(HttpServletResponse.SC_CREATED);
            }
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
    }

    @Test
    void doFilterInternal_withNullContentTypeAndEmptyBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/expenses/1");
        request.setMethod("GET");
        // No query string, no content type

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void doFilterInternal_withLargeResponseBody_truncatesLog() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/expenses/large");
        request.setMethod("GET");

        String largeBody = "A".repeat(1500);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            res.getOutputStream().write(largeBody.getBytes(StandardCharsets.UTF_8));
            if (res instanceof HttpServletResponse httpServletResponse) {
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            }
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals(largeBody, response.getContentAsString());
    }
}