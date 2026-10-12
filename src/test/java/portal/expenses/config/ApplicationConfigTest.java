package portal.expenses.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import portal.expenses.service.UserDetailsServiceImpl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void testPasswordEncoder() {
        ApplicationConfig applicationConfig = new ApplicationConfig(userDetailsService);
        PasswordEncoder encoder = applicationConfig.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void testAuthenticationProvider() {
        ApplicationConfig applicationConfig = new ApplicationConfig(userDetailsService);
        AuthenticationProvider provider = applicationConfig.authenticationProvider();

        assertNotNull(provider);
        assertTrue(provider instanceof DaoAuthenticationProvider);
    }
}