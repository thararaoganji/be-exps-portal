package portal.expenses.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
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

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    void shouldNotFilter_nonActuatorPath_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/expenses");

        boolean result = filter.shouldNotFilter(request);

        assertFalse(result);
    }

    @Test
    void doFilterInternal_jsonRequestAndResponse_logsAndCopiesBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/expenses");
        request.setQueryString("page=1&size=10");
        request.addHeader("Authorization", "Bearer token123");
        request.setContentType("application/json");
        request.setContent("{\"amount\": 100.0}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            req.getInputStream().readAllBytes();
            res.setContentType("application/json");
            res.getOutputStream().write("{\"status\": \"success\"}".getBytes(StandardCharsets.UTF_8));
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals("{\"status\": \"success\"}", response.getContentAsString());
    }

    @Test
    void doFilterInternal_multipartRequest_logsMultipartPlaceholder() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/expenses/receipt");
        request.setContentType("multipart/form-data; boundary=----WebKitFormBoundary");
        request.addHeader("Content-Type", "multipart/form-data");

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            if (res instanceof HttpServletResponse httpServletResponse) {
                httpServletResponse.setStatus(200);
            }
            res.getWriter().write("ok");
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals("ok", response.getContentAsString());
    }

    @Test
    void doFilterInternal_longResponseBody_truncatesLog() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String longResponse = "A".repeat(1500);

        FilterChain filterChain = (req, res) -> {
            if (res instanceof HttpServletResponse httpServletResponse) {
                httpServletResponse.setStatus(200);
            }
            res.getOutputStream().write(longResponse.getBytes(StandardCharsets.UTF_8));
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(longResponse, response.getContentAsString());
    }

    @Test
    void doFilterInternal_noContentTypeAndNoQueryString_executesSuccessfully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
    }
}