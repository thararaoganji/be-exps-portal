package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseApprovalTest {

    @Test
    void testGettersAndSetters() {
        ExpenseApproval approval = new ExpenseApproval();

        approval.setId(10L);
        assertEquals(10L, approval.getId());

        Expense expense = new Expense();
        approval.setExpense(expense);
        assertEquals(expense, approval.getExpense());

        approval.setApprovalLevel("MANAGER");
        assertEquals("MANAGER", approval.getApprovalLevel());

        if (ApprovalStatus.values().length > 0) {
            ApprovalStatus status = ApprovalStatus.values()[0];
            approval.setStatus(status);
            assertEquals(status, approval.getStatus());
        }

        AppUser approver = new AppUser();
        approval.setApprover(approver);
        assertEquals(approver, approval.getApprover());

        approval.setComments("Looks good");
        assertEquals("Looks good", approval.getComments());

        PolicyRule rule = new PolicyRule();
        approval.setAppliedRule(rule);
        assertEquals(rule, approval.getAppliedRule());
    }

    @Test
    void testLifecycleCallbacks() {
        ExpenseApproval approval = new ExpenseApproval();

        assertNull(approval.getCreatedAt());
        assertNull(approval.getUpdatedAt());

        approval.onCreate();
        assertNotNull(approval.getCreatedAt());
        assertNotNull(approval.getUpdatedAt());

        java.time.LocalDateTime firstUpdate = approval.getUpdatedAt();
        approval.onUpdate();

        assertNotNull(approval.getUpdatedAt());
        assertTrue(approval.getUpdatedAt().isAfter(firstUpdate) || approval.getUpdatedAt().isEqual(firstUpdate));
    }
}