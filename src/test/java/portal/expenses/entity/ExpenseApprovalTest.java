package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseApprovalTest {

    @Test
    void testGettersAndSetters() {
        ExpenseApproval approval = new ExpenseApproval();

        Long id = 123L;
        approval.setId(id);
        assertEquals(id, approval.getId());

        Expense expense = new Expense();
        approval.setExpense(expense);
        assertSame(expense, approval.getExpense());

        String approvalLevel = "MANAGER";
        approval.setApprovalLevel(approvalLevel);
        assertEquals(approvalLevel, approval.getApprovalLevel());

        ApprovalStatus status = ApprovalStatus.PENDING;
        approval.setStatus(status);
        assertEquals(status, approval.getStatus());

        AppUser approver = new AppUser();
        approval.setApprover(approver);
        assertSame(approver, approval.getApprover());

        String comments = "Looks good to me";
        approval.setComments(comments);
        assertEquals(comments, approval.getComments());

        PolicyRule appliedRule = new PolicyRule();
        approval.setAppliedRule(appliedRule);
        assertSame(appliedRule, approval.getAppliedRule());
    }

    @Test
    void testLifecycleCallbacks() {
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