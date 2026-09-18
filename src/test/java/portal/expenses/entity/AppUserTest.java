package portal.expenses.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
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
        assertEquals(1L, user.getId());

        user.setUsername("testuser");
        assertEquals("testuser", user.getUsername());

        user.setPassword("secretPass");
        assertEquals("secretPass", user.getPassword());

        user.setName("John Doe");
        assertEquals("John Doe", user.getName());

        user.setEmail("john@example.com");
        assertEquals("john@example.com", user.getEmail());

        Role role1 = mock(Role.class);
        Set<Role> roles = new HashSet<>();
        roles.add(role1);
        user.setRoles(roles);
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

        Role roleAdmin = mock(Role.class);
        when(roleAdmin.getName()).thenReturn("ROLE_ADMIN");

        Role roleUser = mock(Role.class);
        when(roleUser.getName()).thenReturn("ROLE_USER");

        Set<Role> roles = new HashSet<>();
        roles.add(roleAdmin);
        roles.add(roleUser);
        user.setRoles(roles);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
        assertTrue(authorities.stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
    }

    @Test
    void testGetAuthoritiesWhenRolesEmpty() {
        AppUser user = new AppUser();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }
}