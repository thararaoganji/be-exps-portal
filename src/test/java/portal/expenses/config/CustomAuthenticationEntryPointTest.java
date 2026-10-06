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

    private CustomAuthenticationEntryPoint entryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint();
    }

    @Test
    void testCommenceWithAuthHeaderPresent() throws IOException, ServletException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(response.getWriter()).thenReturn(printWriter);

        AuthenticationException authException = new BadCredentialsException("Invalid token");

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"error\": \"Unauthorized\""));
        assertTrue(responseBody.contains("\"path\": \"/api/protected\""));
    }

    @Test
    void testCommenceWithoutAuthHeader() throws IOException, ServletException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        AuthenticationException authException = new BadCredentialsException("No credentials provided");

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"error\": \"Unauthorized\""));
        assertTrue(responseBody.contains("\"path\": \"/api/secure\""));
    }
}