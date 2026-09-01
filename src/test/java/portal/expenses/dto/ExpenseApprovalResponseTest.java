package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExpenseApprovalResponseTest {

    @Test
    void testGettersAndSetters() {
        ExpenseApprovalResponse response = new ExpenseApprovalResponse();

        assertNull(response.getId());
        assertNull(response.getExpenseId());
        assertNull(response.getApprovalLevel());
        assertNull(response.getStatus());
        assertNull(response.getApproverUsername());
        assertNull(response.getComments());
        assertNull(response.getAppliedRuleName());
        assertNull(response.getCreatedAt());

        Long id = 1L;
        Long expenseId = 100L;
        String approvalLevel = "LEVEL_1";
        ApprovalStatus status = null;
        String approverUsername = "manager1";
        String comments = "Approved for travel";
        String appliedRuleName = "Travel Policy Rule";
        LocalDateTime now = LocalDateTime.now();

        response.setId(id);
        response.setExpenseId(expenseId);
        response.setApprovalLevel(approvalLevel);
        response.setStatus(status);
        response.setApproverUsername(approverUsername);
        response.setComments(comments);
        response.setAppliedRuleName(appliedRuleName);
        response.setCreatedAt(now);

        assertEquals(id, response.getId());
        assertEquals(expenseId, response.getExpenseId());
        assertEquals(approvalLevel, response.getApprovalLevel());
        assertEquals(status, response.getStatus());
        assertEquals(approverUsername, response.getApproverUsername());
        assertEquals(comments, response.getComments());
        assertEquals(appliedRuleName, response.getAppliedRuleName());
        assertEquals(now, response.getCreatedAt());
    }
}