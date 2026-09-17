package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseApprovalTest {

    @Test
    void testGettersAndSetters() {
        ExpenseApproval approval = new ExpenseApproval();

        Long id = 100L;
        approval.setId(id);
        assertEquals(id, approval.getId());

        Expense expense = new Expense();
        approval.setExpense(expense);
        assertSame(expense, approval.getExpense());

        String approvalLevel = "MANAGER";
        approval.setApprovalLevel(approvalLevel);
        assertEquals(approvalLevel, approval.getApprovalLevel());

        approval.setStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, approval.getStatus());

        AppUser approver = new AppUser();
        approval.setApprover(approver);
        assertSame(approver, approval.getApprover());

        String comments = "Looks good";
        approval.setComments(comments);
        assertEquals(comments, approval.getComments());

        PolicyRule rule = new PolicyRule();
        approval.setAppliedRule(rule);
        assertSame(rule, approval.getAppliedRule());
    }

    @Test
    void testPrePersistAndPreUpdateLifecycleHooks() {
        ExpenseApproval approval = new ExpenseApproval();

        assertNull(approval.getCreatedAt());
        assertNull(approval.getUpdatedAt());

        approval.onCreate();
        assertNotNull(approval.getCreatedAt());
        assertNotNull(approval.getUpdatedAt());

        var initialUpdatedAt = approval.getUpdatedAt();

        approval.onUpdate();
        assertNotNull(approval.getUpdatedAt());
        assertTrue(!approval.getUpdatedAt().isBefore(initialUpdatedAt));
    }
}