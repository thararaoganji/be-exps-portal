package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditEntryTest {

    @Test
    void testGettersAndSetters() {
        AuditEntry auditEntry = new AuditEntry();

        auditEntry.setId(1L);
        assertEquals(1L, auditEntry.getId());

        Expense expense = new Expense();
        auditEntry.setExpense(expense);
        assertSame(expense, auditEntry.getExpense());

        AppUser user = new AppUser();
        auditEntry.setUser(user);
        assertSame(user, auditEntry.getUser());

        auditEntry.setAction("SUBMITTED");
        assertEquals("SUBMITTED", auditEntry.getAction());

        auditEntry.setDetails("Details about action");
        assertEquals("Details about action", auditEntry.getDetails());

        auditEntry.setOldStatus("DRAFT");
        assertEquals("DRAFT", auditEntry.getOldStatus());

        auditEntry.setNewStatus("PENDING");
        assertEquals("PENDING", auditEntry.getNewStatus());

        auditEntry.setComment("Expense submitted for review");
        assertEquals("Expense submitted for review", auditEntry.getComment());

        auditEntry.setPerformedBy("john.doe");
        assertEquals("john.doe", auditEntry.getPerformedBy());
    }

    @Test
    void testOnCreateSetsCreatedAt() {
        AuditEntry auditEntry = new AuditEntry();
        assertNull(auditEntry.getCreatedAt());

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        auditEntry.onCreate();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(auditEntry.getCreatedAt());
        assertTrue(auditEntry.getCreatedAt().isAfter(before));
        assertTrue(auditEntry.getCreatedAt().isBefore(after));
    }
}