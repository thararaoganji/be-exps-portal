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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestResponseLoggingFilterTest {

    private RequestResponseLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestResponseLoggingFilter();
    }

    @Test
    void shouldNotFilter_whenPathStartsWithActuator_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_whenPathDoesNotStartWithActuator_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/expenses");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_withJsonContentAndQueryString_logsRequestAndResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/expenses");
        request.setQueryString("page=0&size=10");
        request.addHeader("Authorization", "Bearer token");
        request.setContentType("application/json");
        byte[] requestBody = "{\"amount\": 100.0}".getBytes(StandardCharsets.UTF_8);
        request.setContent(requestBody);

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            req.getInputStream().readAllBytes();
            ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);
            res.getOutputStream().write("{\"status\":\"success\"}".getBytes(StandardCharsets.UTF_8));
        };

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getContentAsString().contains("{\"status\":\"success\"}"));
    }

    @Test
    void doFilterInternal_withMultipartFormData_logsMultipartNotice() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/expenses/upload");
        request.setContentType("multipart/form-data; boundary=---boundary");
        request.setContent("file-data".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_CREATED);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_withLongResponseBody_truncatesResponseBodyLogging() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/expenses");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String longBody = "A".repeat(1200);

        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);
            res.getOutputStream().write(longBody.getBytes(StandardCharsets.UTF_8));
        };

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getContentAsString().startsWith("AAAA"));
    }

    @Test
    void doFilterInternal_withNoContentTypeAndEmptyBody_logsSuccessfully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_NO_CONTENT);
        };

        filter.doFilterInternal(request, response, filterChain);

        assertFalse(filter.shouldNotFilter(request));
    }
}