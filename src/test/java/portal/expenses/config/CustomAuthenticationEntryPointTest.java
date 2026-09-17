package portal.expenses.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private CustomAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint();
    }

    @Test
    void commence_WithAuthHeader_SetsUnauthorizedStatusAndJsonResponse() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/v1/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        AuthenticationException authException = new BadCredentialsException("Invalid token");

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"error\": \"Unauthorized\""));
        assertTrue(responseBody.contains("\"message\": \"Access is denied due to invalid credentials\""));
        assertTrue(responseBody.contains("\"path\": \"/api/v1/protected\""));
    }

    @Test
    void commence_WithoutAuthHeader_SetsUnauthorizedStatusAndJsonResponse() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/v1/expenses");
        when(request.getHeader("Authorization")).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        AuthenticationException authException = new BadCredentialsException("Full authentication is required");

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"error\": \"Unauthorized\""));
        assertTrue(responseBody.contains("\"message\": \"Access is denied due to invalid credentials\""));
        assertTrue(responseBody.contains("\"path\": \"/api/v1/expenses\""));
    }
}