package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditEntryTest {

    @Test
    void testGettersAndSetters() {
        AuditEntry auditEntry = new AuditEntry();

        Long id = 1L;
        Expense expense = new Expense();
        AppUser user = new AppUser();
        String action = "APPROVE";
        String details = "Approved by manager";
        String oldStatus = "PENDING";
        String newStatus = "APPROVED";
        String comment = "Looks good";
        String performedBy = "admin";

        auditEntry.setId(id);
        auditEntry.setExpense(expense);
        auditEntry.setUser(user);
        auditEntry.setAction(action);
        auditEntry.setDetails(details);
        auditEntry.setOldStatus(oldStatus);
        auditEntry.setNewStatus(newStatus);
        auditEntry.setComment(comment);
        auditEntry.setPerformedBy(performedBy);

        assertEquals(id, auditEntry.getId());
        assertEquals(expense, auditEntry.getExpense());
        assertEquals(user, auditEntry.getUser());
        assertEquals(action, auditEntry.getAction());
        assertEquals(details, auditEntry.getDetails());
        assertEquals(oldStatus, auditEntry.getOldStatus());
        assertEquals(newStatus, auditEntry.getNewStatus());
        assertEquals(comment, auditEntry.getComment());
        assertEquals(performedBy, auditEntry.getPerformedBy());
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