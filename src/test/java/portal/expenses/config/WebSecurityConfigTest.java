package portal.expenses.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
        AuthenticationManager expectedAuthManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(expectedAuthManager);

        AuthenticationManager actualAuthManager = webSecurityConfig.authenticationManager(authConfig);

        assertNotNull(actualAuthManager);
        assertEquals(expectedAuthManager, actualAuthManager);
    }

    @Test
    void testCorsConfigurationSource() {
        CorsConfigurationSource source = webSecurityConfig.corsConfigurationSource();
        assertNotNull(source);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/expenses/test");
        request.setServletPath("/expenses/test");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config);
        assertTrue(config.getAllowedOrigins().contains("https://du46cvjytrhz9.cloudfront.net"));
        assertTrue(config.getAllowedOrigins().contains("http://localhost:4200"));
        assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(config.getAllowedMethods().contains("GET"));
        assertTrue(config.getAllowedMethods().contains("POST"));
        assertTrue(config.getAllowedHeaders().contains("*"));
        assertTrue(config.getExposedHeaders().contains("Authorization"));
        assertEquals(Boolean.TRUE, config.getAllowCredentials());
        assertEquals(3600L, config.getMaxAge());
    }

    @Test
    void testActuatorSecurity() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class);
        when(http.securityMatcher("/actuator/**")).thenReturn(http);

        when(http.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> customizer =
                    invocation.getArgument(0);
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry =
                    mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class, Answers.RETURNS_DEEP_STUBS);
            customizer.customize(registry);
            return http;
        });

        when(http.csrf(any())).thenAnswer(invocation -> {
            Customizer<CsrfConfigurer<HttpSecurity>> customizer = invocation.getArgument(0);
            CsrfConfigurer<HttpSecurity> csrfConfigurer = mock(CsrfConfigurer.class, Answers.RETURNS_DEEP_STUBS);
            customizer.customize(csrfConfigurer);
            return http;
        });

        when(http.sessionManagement(any())).thenAnswer(invocation -> {
            Customizer<SessionManagementConfigurer<HttpSecurity>> customizer = invocation.getArgument(0);
            SessionManagementConfigurer<HttpSecurity> sessionConfigurer = mock(SessionManagementConfigurer.class, Answers.RETURNS_DEEP_STUBS);
            customizer.customize(sessionConfigurer);
            return http;
        });

        DefaultSecurityFilterChain mockChain = mock(DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(mockChain);

        SecurityFilterChain chain = webSecurityConfig.actuatorSecurity(http);
        assertNotNull(chain);
    }

    @Test
    void testAppSecurity() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class);

        when(http.csrf(any())).thenAnswer(invocation -> {
            Customizer<CsrfConfigurer<HttpSecurity>> customizer = invocation.getArgument(0);
            CsrfConfigurer<HttpSecurity> csrfConfigurer = mock(CsrfConfigurer.class, Answers.RETURNS_DEEP_STUBS);
            customizer.customize(csrfConfigurer);
            return http;
        });

        when(http.cors(any())).thenAnswer(invocation -> {
            Customizer<CorsConfigurer<HttpSecurity>> customizer = invocation.getArgument(0);
            CorsConfigurer<HttpSecurity> corsConfigurer = mock(CorsConfigurer.class, Answers.RETURNS_DEEP_STUBS);
            customizer.customize(corsConfigurer);
            return http;
        });

        when(http.sessionManagement(any())).thenAnswer(invocation -> {
            Customizer<SessionManagementConfigurer<HttpSecurity>> customizer = invocation.getArgument(0);
            SessionManagementConfigurer<HttpSecurity> sessionConfigurer = mock(SessionManagementConfigurer.class, Answers.RETURNS_DEEP_STUBS);
            customizer.customize(sessionConfigurer);
            return http;
        });

        when(http.exceptionHandling(any())).thenAnswer(invocation -> {
            Customizer<ExceptionHandlingConfigurer<HttpSecurity>> customizer = invocation.getArgument(0);
            ExceptionHandlingConfigurer<HttpSecurity> exceptionConfigurer = mock(ExceptionHandlingConfigurer.class, Answers.RETURNS_DEEP_STUBS);
            customizer.customize(exceptionConfigurer);
            return http;
        });

        when(http.authenticationProvider(any())).thenReturn(http);

        when(http.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> customizer =
                    invocation.getArgument(0);
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry =
                    mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class, Answers.RETURNS_DEEP_STUBS);
            customizer.customize(registry);
            return http;
        });

        when(http.addFilterBefore(any(), any())).thenReturn(http);

        DefaultSecurityFilterChain mockChain = mock(DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(mockChain);

        SecurityFilterChain chain = webSecurityConfig.appSecurity(http);
        assertNotNull(chain);
    }
}