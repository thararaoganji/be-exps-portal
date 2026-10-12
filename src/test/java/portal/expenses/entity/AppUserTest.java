package portal.expenses.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserTest {

    @Test
    void testGettersAndSetters() {
        AppUser user = new AppUser();

        user.setId(1L);
        user.setUsername("john_doe");
        user.setPassword("secretPassword");
        user.setName("John Doe");
        user.setEmail("john@example.com");

        Role role = mock(Role.class);
        Set<Role> roles = Collections.singleton(role);
        user.setRoles(roles);

        assertEquals(1L, user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals("secretPassword", user.getPassword());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals(roles, user.getRoles());
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

        user.setRoles(Set.of(role1, role2));

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
        assertTrue(authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }

    @Test
    void testGetAuthoritiesWithEmptyRoles() {
        AppUser user = new AppUser();
        user.setRoles(Collections.emptySet());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }
}