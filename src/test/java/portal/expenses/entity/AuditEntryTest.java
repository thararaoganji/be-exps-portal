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
        assertEquals(expense, auditEntry.getExpense());

        AppUser user = new AppUser();
        auditEntry.setUser(user);
        assertEquals(user, auditEntry.getUser());

        auditEntry.setAction("SUBMITTED");
        assertEquals("SUBMITTED", auditEntry.getAction());

        auditEntry.setDetails("Submitted expense report");
        assertEquals("Submitted expense report", auditEntry.getDetails());

        auditEntry.setOldStatus("DRAFT");
        assertEquals("DRAFT", auditEntry.getOldStatus());

        auditEntry.setNewStatus("PENDING");
        assertEquals("PENDING", auditEntry.getNewStatus());

        auditEntry.setComment("Ready for review");
        assertEquals("Ready for review", auditEntry.getComment());

        auditEntry.setPerformedBy("user@example.com");
        assertEquals("user@example.com", auditEntry.getPerformedBy());
    }

    @Test
    void testOnCreate() {
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