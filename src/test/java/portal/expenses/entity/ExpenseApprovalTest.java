package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ExpenseApprovalTest {

    @Test
    void testGettersAndSetters() {
        ExpenseApproval approval = new ExpenseApproval();

        approval.setId(10L);
        assertEquals(10L, approval.getId());

        Expense expense = mock(Expense.class);
        approval.setExpense(expense);
        assertSame(expense, approval.getExpense());

        approval.setApprovalLevel("MANAGER");
        assertEquals("MANAGER", approval.getApprovalLevel());

        ApprovalStatus status = mock(ApprovalStatus.class);
        approval.setStatus(status);
        assertSame(status, approval.getStatus());

        AppUser approver = mock(AppUser.class);
        approval.setApprover(approver);
        assertSame(approver, approval.getApprover());

        approval.setComments("Looks good");
        assertEquals("Looks good", approval.getComments());

        PolicyRule rule = mock(PolicyRule.class);
        approval.setAppliedRule(rule);
        assertSame(rule, approval.getAppliedRule());
    }

    @Test
    void testPrePersistAndPreUpdate() {
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