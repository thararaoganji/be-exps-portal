package portal.expenses.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import portal.expenses.util.JwtUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_NoAuthHeader_PassesToFilterChain() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_AuthHeaderNotBearer_PassesToFilterChain() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_InvalidJwtToken_Returns403Forbidden() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.value");
        when(jwtUtil.extractUsername("invalid.token.value")).thenThrow(new JwtException("Token expired"));

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        assertTrue(stringWriter.toString().contains("Invalid or expired JWT token"));
        assertTrue(stringWriter.toString().contains("Token expired"));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @SuppressWarnings("unchecked")
    void doFilterInternal_ValidToken_UserNotAuthenticated_AuthenticatesSuccessfully() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "testuser";
        Collection authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);
        doReturn(authorities).when(userDetails).getAuthorities();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertSame(userDetails, authentication.getPrincipal());
        assertEquals(1, authentication.getAuthorities().size());
    }

    @Test
    void doFilterInternal_TokenValidationFailed_DoesNotSetAuthentication() throws ServletException, IOException {
        String token = "valid.format.invalid.signature";
        String username = "testuser";

        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_UserAlreadyAuthenticated_DoesNotReauthenticate() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "testuser";

        Authentication existingAuth = new UsernamePasswordAuthenticationToken("existingUser", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertSame(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_ExtractUsernameNull_DoesNotAttemptAuth() throws ServletException, IOException {
        String token = "token.with.null.user";

        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_ExceptionDuringUserDetailsService_CatchesAndContinuesFilterChain() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "testuser";

        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenThrow(new RuntimeException("User service down"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}