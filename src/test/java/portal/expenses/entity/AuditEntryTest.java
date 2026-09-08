package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditEntryTest {

    @Test
    void testGettersAndSetters() {
        AuditEntry auditEntry = new AuditEntry();

        Long id = 100L;
        Expense expense = new Expense();
        AppUser user = new AppUser();
        String action = "APPROVE";
        String oldStatus = "PENDING";
        String newStatus = "APPROVED";
        String comment = "Approved by manager";
        String performedBy = "manager@example.com";
        String details = "Details of the audit";

        auditEntry.setId(id);
        auditEntry.setExpense(expense);
        auditEntry.setUser(user);
        auditEntry.setAction(action);
        auditEntry.setOldStatus(oldStatus);
        auditEntry.setNewStatus(newStatus);
        auditEntry.setComment(comment);
        auditEntry.setPerformedBy(performedBy);
        auditEntry.setDetails(details);

        assertEquals(id, auditEntry.getId());
        assertEquals(expense, auditEntry.getExpense());
        assertEquals(user, auditEntry.getUser());
        assertEquals(action, auditEntry.getAction());
        assertEquals(oldStatus, auditEntry.getOldStatus());
        assertEquals(newStatus, auditEntry.getNewStatus());
        assertEquals(comment, auditEntry.getComment());
        assertEquals(performedBy, auditEntry.getPerformedBy());
        assertEquals(details, auditEntry.getDetails());
    }

    @Test
    void testOnCreate() {
        AuditEntry auditEntry = new AuditEntry();
        assertNull(auditEntry.getCreatedAt());

        auditEntry.onCreate();

        assertNotNull(auditEntry.getCreatedAt());
    }
}