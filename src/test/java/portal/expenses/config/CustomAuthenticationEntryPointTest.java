package portal.expenses.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    private CustomAuthenticationEntryPoint entryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint();
    }

    @Test
    void testCommence_withoutAuthHeader() throws IOException, ServletException {
        String uri = "/api/expenses";
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(authException.getMessage()).thenReturn("Full authentication is required");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"error\": \"Unauthorized\""));
        assertTrue(responseBody.contains("\"message\": \"Access is denied due to invalid credentials\""));
        assertTrue(responseBody.contains("\"path\": \"/api/expenses\""));
    }

    @Test
    void testCommence_withAuthHeader() throws IOException, ServletException {
        String uri = "/api/reports";
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(authException.getMessage()).thenReturn("Token expired");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"error\": \"Unauthorized\""));
        assertTrue(responseBody.contains("\"message\": \"Access is denied due to invalid credentials\""));
        assertTrue(responseBody.contains("\"path\": \"/api/reports\""));
    }
}