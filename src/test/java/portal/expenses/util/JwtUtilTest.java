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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "0123456789012345678901234567890123456789";
    private static final long EXPIRATION = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", EXPIRATION);
    }

    @Test
    void testGenerateAndValidateToken_Success() {
        UserDetails userDetails = new User("testuser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);

        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);

        boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidUsername() {
        UserDetails userDetails1 = new User("testuser1", "password", Collections.emptyList());
        UserDetails userDetails2 = new User("testuser2", "password", Collections.emptyList());

        String token = jwtUtil.generateToken(userDetails1);

        boolean isValid = jwtUtil.validateToken(token, userDetails2);
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1000L);
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());

        String token = jwtUtil.generateToken(userDetails);
        assertThrows(Exception.class, () -> jwtUtil.validateToken(token, userDetails));
    }
}