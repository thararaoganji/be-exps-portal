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
        user.setPassword("secret");
        user.setName("Test User");
        user.setEmail("test@example.com");

        Set<Role> roles = new HashSet<>();
        user.setRoles(roles);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertSame(roles, user.getRoles());
    }

    @Test
    void testUserDetailsStatusFlags() {
        AppUser user = new AppUser();

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void testGetAuthorities() {
        AppUser user = new AppUser();

        Role role1 = mock(Role.class);
        when(role1.getName()).thenReturn("ROLE_USER");

        Role role2 = mock(Role.class);
        when(role2.getName()).thenReturn("ROLE_ADMIN");

        Set<Role> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        user.setRoles(roles);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testGetAuthoritiesEmptyRoles() {
        AppUser user = new AppUser();
        user.setRoles(Collections.emptySet());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }
}