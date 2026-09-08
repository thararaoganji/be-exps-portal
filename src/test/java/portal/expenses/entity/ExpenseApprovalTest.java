package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseApprovalTest {

    @Test
    void testGettersAndSetters() {
        ExpenseApproval approval = new ExpenseApproval();

        Long id = 1L;
        Expense expense = new Expense();
        String approvalLevel = "MANAGER";
        ApprovalStatus status = ApprovalStatus.PENDING;
        AppUser approver = new AppUser();
        String comments = "Looks good";
        PolicyRule appliedRule = new PolicyRule();

        approval.setId(id);
        approval.setExpense(expense);
        approval.setApprovalLevel(approvalLevel);
        approval.setStatus(status);
        approval.setApprover(approver);
        approval.setComments(comments);
        approval.setAppliedRule(appliedRule);

        assertEquals(id, approval.getId());
        assertEquals(expense, approval.getExpense());
        assertEquals(approvalLevel, approval.getApprovalLevel());
        assertEquals(status, approval.getStatus());
        assertEquals(approver, approval.getApprover());
        assertEquals(comments, approval.getComments());
        assertEquals(appliedRule, approval.getAppliedRule());
    }

    @Test
    void testPrePersistAndPreUpdateLifecycleCallbacks() {
        ExpenseApproval approval = new ExpenseApproval();

        assertNull(approval.getCreatedAt());
        assertNull(approval.getUpdatedAt());

        approval.onCreate();
        assertNotNull(approval.getCreatedAt());
        assertNotNull(approval.getUpdatedAt());

        approval.onUpdate();
        assertNotNull(approval.getUpdatedAt());
    }
}