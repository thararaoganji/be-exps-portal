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
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(expectedManager);

        AuthenticationManager actualManager = webSecurityConfig.authenticationManager(authConfig);

        assertEquals(expectedManager, actualManager);
        verify(authConfig).getAuthenticationManager();
    }

    @Test
    void testCorsConfigurationSource() {
        CorsConfigurationSource source = webSecurityConfig.corsConfigurationSource();
        assertNotNull(source);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");
        CorsConfiguration corsConfig = source.getCorsConfiguration(request);

        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedOrigins().contains("https://du46cvjytrhz9.cloudfront.net"));
        assertTrue(corsConfig.getAllowedOrigins().contains("http://localhost:4200"));
        assertTrue(corsConfig.getAllowedOrigins().contains("http://localhost:3000"));

        assertTrue(corsConfig.getAllowedMethods().contains("GET"));
        assertTrue(corsConfig.getAllowedMethods().contains("POST"));
        assertTrue(corsConfig.getAllowedMethods().contains("PUT"));
        assertTrue(corsConfig.getAllowedMethods().contains("DELETE"));
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
        assertTrue(corsConfig.getAllowedMethods().contains("PATCH"));
        assertTrue(corsConfig.getAllowedMethods().contains("HEAD"));

        assertTrue(corsConfig.getAllowedHeaders().contains("*"));
        assertTrue(corsConfig.getExposedHeaders().contains("Authorization"));
        assertEquals(Boolean.TRUE, corsConfig.getAllowCredentials());
        assertEquals(3600L, corsConfig.getMaxAge());
    }
}