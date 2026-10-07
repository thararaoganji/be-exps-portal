package portal.expenses.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomAuthenticationEntryPointTest {

    private CustomAuthenticationEntryPoint entryPoint;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        entryPoint = new CustomAuthenticationEntryPoint();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void commence_withAuthorizationHeaderPresent() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/test/resource");
        when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
        AuthenticationException authException = new BadCredentialsException("Invalid token");

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        String body = stringWriter.toString();
        assertTrue(body.contains("\"error\": \"Unauthorized\""));
        assertTrue(body.contains("\"message\": \"Access is denied due to invalid credentials\""));
        assertTrue(body.contains("\"path\": \"/api/test/resource\""));
    }

    @Test
    void commence_withoutAuthorizationHeader() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/other/path");
        when(request.getHeader("Authorization")).thenReturn(null);
        AuthenticationException authException = new BadCredentialsException("Full authentication is required");

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        String body = stringWriter.toString();
        assertTrue(body.contains("\"error\": \"Unauthorized\""));
        assertTrue(body.contains("\"message\": \"Access is denied due to invalid credentials\""));
        assertTrue(body.contains("\"path\": \"/api/other/path\""));
    }
}