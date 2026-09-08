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

    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        authenticationEntryPoint = new CustomAuthenticationEntryPoint();
    }

    @Test
    void commence_withAuthorizationHeader_writesUnauthorizedResponse() throws IOException, ServletException {
        String requestUri = "/api/protected";
        String authHeader = "Bearer invalid-token";
        AuthenticationException authException = new BadCredentialsException("Invalid token");

        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        authenticationEntryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"error\": \"Unauthorized\""));
        assertTrue(responseBody.contains("\"message\": \"Access is denied due to invalid credentials\""));
        assertTrue(responseBody.contains("\"path\": \"/api/protected\""));
    }

    @Test
    void commence_withoutAuthorizationHeader_writesUnauthorizedResponse() throws IOException, ServletException {
        String requestUri = "/api/data";
        AuthenticationException authException = new BadCredentialsException("Full authentication is required");

        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getHeader("Authorization")).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        authenticationEntryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"error\": \"Unauthorized\""));
        assertTrue(responseBody.contains("\"message\": \"Access is denied due to invalid credentials\""));
        assertTrue(responseBody.contains("\"path\": \"/api/data\""));
    }
}