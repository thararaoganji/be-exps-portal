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

        auditEntry.setDetails("Submitted new expense report");
        assertEquals("Submitted new expense report", auditEntry.getDetails());

        auditEntry.setOldStatus("DRAFT");
        assertEquals("DRAFT", auditEntry.getOldStatus());

        auditEntry.setNewStatus("PENDING");
        assertEquals("PENDING", auditEntry.getNewStatus());

        auditEntry.setComment("Ready for review");
        assertEquals("Ready for review", auditEntry.getComment());

        auditEntry.setPerformedBy("john.doe");
        assertEquals("john.doe", auditEntry.getPerformedBy());
    }

    @Test
    void testOnCreate() {
        AuditEntry auditEntry = new AuditEntry();
        assertNull(auditEntry.getCreatedAt());

        auditEntry.onCreate();

        assertNotNull(auditEntry.getCreatedAt());
        assertFalse(auditEntry.getCreatedAt().isAfter(LocalDateTime.now()));
    }
}