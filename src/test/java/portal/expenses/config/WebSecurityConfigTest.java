package portal.expenses.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSecurityConfigTest {

    @Mock
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Mock
    private AuthenticationProvider authenticationProvider;

    private WebSecurityConfig webSecurityConfig;

    @BeforeEach
    void setUp() {
        webSecurityConfig = new WebSecurityConfig(jwtAuthFilter, customAuthenticationEntryPoint, authenticationProvider);
    }

    @Test
    void testAuthenticationManager() throws Exception {
        AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);

        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager result = webSecurityConfig.authenticationManager(authenticationConfiguration);

        assertNotNull(result);
        assertEquals(authenticationManager, result);
        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    void testCorsConfigurationSource() {
        CorsConfigurationSource source = webSecurityConfig.corsConfigurationSource();
        assertNotNull(source);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config);
        assertTrue(config.getAllowedOrigins().contains("https://du46cvjytrhz9.cloudfront.net"));
        assertTrue(config.getAllowedOrigins().contains("http://localhost:4200"));
        assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(config.getAllowedMethods().contains("GET"));
        assertTrue(config.getAllowedMethods().contains("POST"));
        assertTrue(config.getAllowedMethods().contains("OPTIONS"));
        assertTrue(config.getAllowedHeaders().contains("*"));
        assertTrue(config.getExposedHeaders().contains("Authorization"));
        assertEquals(Boolean.TRUE, config.getAllowCredentials());
        assertEquals(3600L, config.getMaxAge());
    }

    @Test
    void testActuatorSecurity() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        DefaultSecurityFilterChain filterChain = mock(DefaultSecurityFilterChain.class);

        when(http.build()).thenReturn(filterChain);

        SecurityFilterChain result = webSecurityConfig.actuatorSecurity(http);

        assertNotNull(result);
        assertEquals(filterChain, result);
    }

    @Test
    void testAppSecurity() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        DefaultSecurityFilterChain filterChain = mock(DefaultSecurityFilterChain.class);

        when(http.build()).thenReturn(filterChain);

        SecurityFilterChain result = webSecurityConfig.appSecurity(http);

        assertNotNull(result);
        assertEquals(filterChain, result);
    }
}