package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoleTest {

    @Test
    void testGettersAndSetters() {
        Role role = new Role();

        assertNull(role.getId());
        assertNull(role.getName());

        Long id = 1L;
        String name = "ROLE_ADMIN";

        role.setId(id);
        role.setName(name);

        assertEquals(id, role.getId());
        assertEquals(name, role.getName());
    }
}