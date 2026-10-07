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

        user.setId(10L);
        user.setUsername("john_doe");
        user.setPassword("secret123");
        user.setName("John Doe");
        user.setEmail("john@example.com");

        Set<Role> roles = new HashSet<>();
        Role mockRole = mock(Role.class);
        roles.add(mockRole);
        user.setRoles(roles);

        assertEquals(10L, user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals("secret123", user.getPassword());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals(roles, user.getRoles());
    }

    @Test
    void testUserDetailsMethods() {
        AppUser user = new AppUser();
        user.setUsername("jane_doe");
        user.setPassword("pass456");

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
        assertEquals("jane_doe", user.getUsername());
        assertEquals("pass456", user.getPassword());
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
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
        assertTrue(authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }
}