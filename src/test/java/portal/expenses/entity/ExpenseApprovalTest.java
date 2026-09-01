package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseApprovalTest {

    @Test
    void testGettersAndSetters() {
        ExpenseApproval approval = new ExpenseApproval();

        Long id = 1L;
        approval.setId(id);
        assertEquals(id, approval.getId());

        Expense expense = new Expense();
        approval.setExpense(expense);
        assertEquals(expense, approval.getExpense());

        String approvalLevel = "MANAGER";
        approval.setApprovalLevel(approvalLevel);
        assertEquals(approvalLevel, approval.getApprovalLevel());

        ApprovalStatus status = ApprovalStatus.APPROVED;
        approval.setStatus(status);
        assertEquals(status, approval.getStatus());

        AppUser approver = new AppUser();
        approval.setApprover(approver);
        assertEquals(approver, approval.getApprover());

        String comments = "Looks good";
        approval.setComments(comments);
        assertEquals(comments, approval.getComments());

        PolicyRule rule = new PolicyRule();
        approval.setAppliedRule(rule);
        assertEquals(rule, approval.getAppliedRule());
    }

    @Test
    void testPrePersistAndPreUpdate() throws InterruptedException {
        ExpenseApproval approval = new ExpenseApproval();
        assertNull(approval.getCreatedAt());
        assertNull(approval.getUpdatedAt());

        approval.onCreate();
        assertNotNull(approval.getCreatedAt());
        assertNotNull(approval.getUpdatedAt());

        java.time.LocalDateTime firstUpdatedAt = approval.getUpdatedAt();
        Thread.sleep(10);
        approval.onUpdate();
        assertNotNull(approval.getUpdatedAt());
        assertTrue(approval.getUpdatedAt().isAfter(firstUpdatedAt) || approval.getUpdatedAt().isEqual(firstUpdatedAt));
    }
}