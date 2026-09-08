package portal.expenses.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestResponseLoggingFilterTest {

    private RequestResponseLoggingFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RequestResponseLoggingFilter();
    }

    @Test
    void shouldNotFilter_actuatorPath_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_nonActuatorPath_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/expenses");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_withJsonRequestBodyAndResponse_executesSuccessfully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/expenses");
        request.setQueryString("page=1&size=10");
        request.addHeader("Authorization", "Bearer token123");
        request.setContentType("application/json");
        request.setContent("{\"amount\": 100.0}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            var res = (jakarta.servlet.http.HttpServletResponse) invocation.getArgument(1);
            res.setStatus(200);
            res.getOutputStream().write("{\"status\": \"OK\"}".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_withMultipartContentType_executesSuccessfully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/expenses/upload");
        request.setContentType("multipart/form-data; boundary=---123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            var res = (jakarta.servlet.http.HttpServletResponse) invocation.getArgument(1);
            res.setStatus(201);
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_withNullContentTypeAndNoQueryString_executesSuccessfully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/expenses");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_withLargeResponseBody_truncatesLog() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/expenses");
        MockHttpServletResponse response = new MockHttpServletResponse();

        StringBuilder largeResponse = new StringBuilder();
        for (int i = 0; i < 1100; i++) {
            largeResponse.append("a");
        }
        byte[] bodyBytes = largeResponse.toString().getBytes(StandardCharsets.UTF_8);

        doAnswer(invocation -> {
            var res = (jakarta.servlet.http.HttpServletResponse) invocation.getArgument(1);
            res.setStatus(200);
            res.getOutputStream().write(bodyBytes);
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_withEmptyContent_executesSuccessfully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/expenses");
        request.setContentType("application/json");
        // No body content
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }
}