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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import portal.expenses.util.JwtUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void doFilterInternal_noAuthHeader_callsFilterChain() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/expenses");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_authHeaderWithoutBearer_callsFilterChain() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/expenses");
        when(request.getHeader("Authorization")).thenReturn("Basic 12345");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_jwtExceptionOnExtract_returnsForbidden() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/expenses");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(jwtUtil.extractUsername("invalid_token")).thenThrow(new JwtException("Token expired"));

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        String token = "valid_token";
        String username = "testuser";
        UserDetails userDetails = new User(username, "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(request.getRequestURI()).thenReturn("/api/expenses");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(username, ((UserDetails) auth.getPrincipal()).getUsername());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidTokenValidation_doesNotSetAuthentication() throws ServletException, IOException {
        String token = "invalid_token";
        String username = "testuser";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        when(request.getRequestURI()).thenReturn("/api/expenses");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_userDetailsServiceThrowsException_handlesGracefully() throws ServletException, IOException {
        String token = "some_token";
        String username = "testuser";

        when(request.getRequestURI()).thenReturn("/api/expenses");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenThrow(new RuntimeException("User not found"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_alreadyAuthenticated_doesNotReauthenticate() throws ServletException, IOException {
        String token = "some_token";
        String username = "testuser";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        when(request.getRequestURI()).thenReturn("/api/expenses");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);

        // Pre-set authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("alreadyAuth", null, Collections.emptyList())
        );

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtUtil, never()).validateToken(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_usernameIsNull_skipsAuthentication() throws ServletException, IOException {
        String token = "some_token";

        when(request.getRequestURI()).thenReturn("/api/expenses");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(filterChain).doFilter(request, response);
    }
}