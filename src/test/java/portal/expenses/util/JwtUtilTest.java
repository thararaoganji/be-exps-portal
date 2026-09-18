package portal.expenses.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserDetails otherUserDetails;

    // Secret must be at least 256 bits (32 bytes) for HMAC-SHA256
    private final String secret = "1234567890123456789012345678901234567890123456789012345678901234";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", expirationMs);
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(userDetails).getAuthorities();

        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String username = jwtUtil.extractUsername(token);
        assertEquals("john.doe", username);
    }

    @Test
    void testValidateToken_Success() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(userDetails).getAuthorities();

        String token = jwtUtil.generateToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WrongUsername() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        List<GrantedAuthority> authorities = Collections.emptyList();
        doReturn(authorities).when(userDetails).getAuthorities();

        when(otherUserDetails.getUsername()).thenReturn("jane.doe");

        String token = jwtUtil.generateToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token, otherUserDetails);
        assertFalse(isValid);
    }
}