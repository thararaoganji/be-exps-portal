package portal.expenses.entity;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseApprovalTest {

    @Test
    void testGettersAndSetters() {
        ExpenseApproval approval = new ExpenseApproval();

        Long id = 100L;
        approval.setId(id);
        assertEquals(id, approval.getId());

        Expense expense = Mockito.mock(Expense.class);
        approval.setExpense(expense);
        assertEquals(expense, approval.getExpense());

        String approvalLevel = "MANAGER";
        approval.setApprovalLevel(approvalLevel);
        assertEquals(approvalLevel, approval.getApprovalLevel());

        ApprovalStatus status = ApprovalStatus.PENDING;
        approval.setStatus(status);
        assertEquals(status, approval.getStatus());

        AppUser approver = Mockito.mock(AppUser.class);
        approval.setApprover(approver);
        assertEquals(approver, approval.getApprover());

        String comments = "Looks good";
        approval.setComments(comments);
        assertEquals(comments, approval.getComments());

        PolicyRule rule = Mockito.mock(PolicyRule.class);
        approval.setAppliedRule(rule);
        assertEquals(rule, approval.getAppliedRule());
    }

    @Test
    void testLifecycleCallbacks() throws InterruptedException {
        ExpenseApproval approval = new ExpenseApproval();

        assertNull(approval.getCreatedAt());
        assertNull(approval.getUpdatedAt());

        approval.onCreate();
        LocalDateTime createdAt = approval.getCreatedAt();
        LocalDateTime updatedAt = approval.getUpdatedAt();

        assertNotNull(createdAt);
        assertNotNull(updatedAt);

        Thread.sleep(10);
        approval.onUpdate();

        assertEquals(createdAt, approval.getCreatedAt());
        assertTrue(approval.getUpdatedAt().isAfter(updatedAt) || approval.getUpdatedAt().isEqual(updatedAt));
    }
}