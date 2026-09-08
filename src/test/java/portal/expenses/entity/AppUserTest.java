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
        user.setName("Test Name");
        user.setEmail("test@example.com");

        Role mockRole = Mockito.mock(Role.class);
        when(mockRole.getName()).thenReturn("ROLE_USER");
        Set<Role> roles = new HashSet<>(Collections.singletonList(mockRole));
        user.setRoles(roles);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("Test Name", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(roles, user.getRoles());

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
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