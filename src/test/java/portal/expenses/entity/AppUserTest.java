package portal.expenses.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserTest {

    @Test
    void testGettersAndSetters() {
        AppUser user = new AppUser();

        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setName("Test User");
        user.setEmail("test@example.com");

        Role role = mock(Role.class);
        Set<Role> roles = new HashSet<>(Collections.singletonList(role));
        user.setRoles(roles);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(roles, user.getRoles());
    }

    @Test
    void testGetAuthorities() {
        AppUser user = new AppUser();
        Role role = mock(Role.class);
        when(role.getName()).thenReturn("ROLE_ADMIN");

        user.setRoles(Collections.singleton(role));

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    void testUserDetailsBooleanFlags() {
        AppUser user = new AppUser();

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }
}