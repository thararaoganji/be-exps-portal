package portal.expenses.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSecurityConfigTest {

    @Mock
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Mock
    private AuthenticationProvider authenticationProvider;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    private WebSecurityConfig webSecurityConfig;

    @BeforeEach
    void setUp() {
        webSecurityConfig = new WebSecurityConfig(
                jwtAuthFilter,
                customAuthenticationEntryPoint,
                authenticationProvider
        );
    }

    @Test
    void testAuthenticationManager() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager result = webSecurityConfig.authenticationManager(authenticationConfiguration);

        assertNotNull(result);
        assertSame(authenticationManager, result);
        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    void testCorsConfigurationSource() {
        CorsConfigurationSource source = webSecurityConfig.corsConfigurationSource();
        assertNotNull(source);
        assertInstanceOf(UrlBasedCorsConfigurationSource.class, source);

        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        assertNotNull(config);
        assertEquals(Arrays.asList("https://du46cvjytrhz9.cloudfront.net", "http://localhost:4200", "http://localhost:3000"), config.getAllowedOrigins());
        assertEquals(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"), config.getAllowedMethods());
        assertEquals(Arrays.asList("*"), config.getAllowedHeaders());
        assertEquals(Arrays.asList("Authorization"), config.getExposedHeaders());
        assertEquals(Boolean.TRUE, config.getAllowCredentials());
        assertEquals(3600L, config.getMaxAge());
    }

    @Test
    void testActuatorSecurity() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class, Answers.RETURNS_SELF);
        DefaultSecurityFilterChain filterChain = mock(DefaultSecurityFilterChain.class);
        when(httpSecurity.build()).thenReturn(filterChain);

        SecurityFilterChain result = webSecurityConfig.actuatorSecurity(httpSecurity);

        assertNotNull(result);
        assertSame(filterChain, result);
        verify(httpSecurity).securityMatcher("/actuator/**");
        verify(httpSecurity).build();
    }

    @Test
    void testAppSecurity() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class, Answers.RETURNS_SELF);
        DefaultSecurityFilterChain filterChain = mock(DefaultSecurityFilterChain.class);
        when(httpSecurity.build()).thenReturn(filterChain);

        SecurityFilterChain result = webSecurityConfig.appSecurity(httpSecurity);

        assertNotNull(result);
        assertSame(filterChain, result);
        verify(httpSecurity).authenticationProvider(authenticationProvider);
        verify(httpSecurity).build();
    }
}