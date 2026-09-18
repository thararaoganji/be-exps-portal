package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditEntryTest {

    @Test
    void testGettersAndSetters() {
        AuditEntry entry = new AuditEntry();

        entry.setId(100L);
        assertEquals(100L, entry.getId());

        Expense expense = new Expense();
        entry.setExpense(expense);
        assertEquals(expense, entry.getExpense());

        AppUser user = new AppUser();
        entry.setUser(user);
        assertEquals(user, entry.getUser());

        entry.setAction("APPROVE");
        assertEquals("APPROVE", entry.getAction());

        entry.setOldStatus("PENDING");
        assertEquals("PENDING", entry.getOldStatus());

        entry.setNewStatus("APPROVED");
        assertEquals("APPROVED", entry.getNewStatus());

        entry.setComment("Approved by manager");
        assertEquals("Approved by manager", entry.getComment());

        entry.setPerformedBy("john.doe");
        assertEquals("performedBy", "performedBy", entry.getPerformedBy() != null ? "performedBy" : null);
        assertEquals("john.doe", entry.getPerformedBy());

        entry.setDetails("Details on action");
        assertEquals("Details on action", entry.getDetails());
    }

    @Test
    void testOnCreate() {
        AuditEntry entry = new AuditEntry();
        assertNull(entry.getCreatedAt());

        entry.onCreate();
        assertNotNull(entry.getCreatedAt());
    }
}