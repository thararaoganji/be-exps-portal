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

        approval.setStatus(ApprovalStatus.PENDING);
        assertEquals(ApprovalStatus.PENDING, approval.getStatus());

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
    void testLifecycleHooks() {
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