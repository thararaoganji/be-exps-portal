package portal.expenses.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // HMAC-SHA256 key must be at least 256 bits (32 bytes)
    private static final String SECRET_KEY = "1234567890123456789012345678901234567890123456789012345678901234";
    private static final long EXPIRATION_MS = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", EXPIRATION_MS);
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        UserDetails userDetails = new User("testuser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testValidateTokenSuccess() {
        UserDetails userDetails = new User("alice", "password", Collections.emptyList());

        String token = jwtUtil.generateToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenMismatchedUsername() {
        UserDetails userDetails = new User("alice", "password", Collections.emptyList());
        UserDetails otherUser = new User("bob", "password", Collections.emptyList());

        String token = jwtUtil.generateToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token, otherUser);
        assertFalse(isValid);
    }
}