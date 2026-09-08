package portal.expenses.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
    void testDoFilterInternal_WithStandardRequestAndResponseBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/expenses");
        request.setMethod("POST");
        request.setQueryString("sort=asc");
        request.addHeader("Content-Type", "application/json");
        request.setContentType("application/json");
        request.setContent("{\"amount\": 100}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        doAnswer(invocation -> {
            HttpServletResponse res = invocation.getArgument(1);
            res.setStatus(HttpServletResponse.SC_OK);
            res.getOutputStream().write("{\"status\": \"success\"}".getBytes());
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
        assertTrue(response.getContentAsString().contains("success"));
    }

    @Test
    void testDoFilterInternal_WithMultipartRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/upload");
        request.setMethod("POST");
        request.setContentType("multipart/form-data; boundary=---123");
        request.setContent("dummy multipart data".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_WithLargeResponseBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/data");
        request.setMethod("GET");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        String largeString = "a".repeat(1500);

        doAnswer(invocation -> {
            HttpServletResponse res = invocation.getArgument(1);
            res.setStatus(HttpServletResponse.SC_OK);
            res.getOutputStream().write(largeString.getBytes());
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
        assertTrue(response.getContentAsString().length() >= 1500);
    }

    @Test
    void testDoFilterInternal_WithNullContentTypeAndNoQueryString() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/empty");
        request.setMethod("GET");
        // No query string, no content type

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }
}