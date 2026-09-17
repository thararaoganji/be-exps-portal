package portal.expenses.entity;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
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

        Set<Role> roles = new HashSet<>();
        user.setRoles(roles);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertSame(roles, user.getRoles());
    }

    @Test
    void testUserDetailsMethods() {
        AppUser user = new AppUser();
        user.setUsername("john_doe");
        user.setPassword("secret");

        assertEquals("john_doe", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void testGetAuthorities() {
        AppUser user = new AppUser();

        Role roleAdmin = Mockito.mock(Role.class);
        when(roleAdmin.getName()).thenReturn("ROLE_ADMIN");

        Role roleUser = Mockito.mock(Role.class);
        when(roleUser.getName()).thenReturn("ROLE_USER");

        Set<Role> roles = new HashSet<>();
        roles.add(roleAdmin);
        roles.add(roleUser);
        user.setRoles(roles);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(2, authorities.size());

        Set<String> authorityNames = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            authorityNames.add(authority.getAuthority());
        }

        assertTrue(authorityNames.contains("ROLE_ADMIN"));
        assertTrue(authorityNames.contains("ROLE_USER"));
    }

    @Test
    void testGetAuthoritiesEmpty() {
        AppUser user = new AppUser();
        user.setRoles(Collections.emptySet());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }
}