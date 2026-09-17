package portal.expenses.config;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
    void testShouldNotFilterForActuatorEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void testShouldNotFilterForNonActuatorEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void testDoFilterInternalWithJsonRequestAndResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/expenses");
        request.setQueryString("page=1&size=10");
        request.addHeader("Authorization", "Bearer token123");
        request.setContentType("application/json");
        byte[] requestBody = "{\"amount\": 100}".getBytes(StandardCharsets.UTF_8);
        request.setContent(requestBody);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws IOException, ServletException {
                // Read request body to populate content cache
                req.getInputStream().readAllBytes();
                res.getOutputStream().write("{\"status\": \"CREATED\"}".getBytes(StandardCharsets.UTF_8));
                super.doFilter(req, res);
            }
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals("{\"status\": \"CREATED\"}", response.getContentAsString());
    }

    @Test
    void testDoFilterInternalWithMultipartRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/expenses/upload");
        request.setContentType("multipart/form-data; boundary=----WebKitFormBoundary");
        request.setContent("dummy-form-data".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws IOException, ServletException {
                req.getInputStream().readAllBytes();
                res.getOutputStream().write("OK".getBytes(StandardCharsets.UTF_8));
                super.doFilter(req, res);
            }
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals("OK", response.getContentAsString());
    }

    @Test
    void testDoFilterInternalWithNullContentTypeAndNoQueryString() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses");
        // No query string, no content type

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertArrayEquals(new byte[0], response.getContentAsByteArray());
    }

    @Test
    void testDoFilterInternalWithLongResponseBodyExceeding1000Chars() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses/all");

        StringBuilder longBodyBuilder = new StringBuilder();
        for (int i = 0; i < 1200; i++) {
            longBodyBuilder.append("a");
        }
        String longBody = longBodyBuilder.toString();

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws IOException, ServletException {
                res.getOutputStream().write(longBody.getBytes(StandardCharsets.UTF_8));
                super.doFilter(req, res);
            }
        };

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(longBody, response.getContentAsString());
    }
}