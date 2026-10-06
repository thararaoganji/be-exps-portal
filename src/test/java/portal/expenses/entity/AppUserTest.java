package portal.expenses.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserTest {

    @Test
    void testGettersAndSetters() {
        AppUser user = new AppUser();

        user.setId(1L);
        user.setUsername("john_doe");
        user.setPassword("secret");
        user.setName("John Doe");
        user.setEmail("john@example.com");

        Set<Role> roles = new HashSet<>();
        user.setRoles(roles);

        assertEquals(1L, user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals(roles, user.getRoles());
    }

    @Test
    void testUserDetailsStatusMethods() {
        AppUser user = new AppUser();

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void testGetAuthorities() {
        AppUser user = new AppUser();
        Role role = mock(Role.class);
        when(role.getName()).thenReturn("ROLE_USER");

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }
}