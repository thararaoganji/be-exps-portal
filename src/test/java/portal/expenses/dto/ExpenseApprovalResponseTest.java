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
        String approvalLevel = "MANAGER";
        String approverUsername = "john_manager";
        String comments = "Approved on review";
        String appliedRuleName = "STANDARD_RULE";
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);

        response.setId(id);
        response.setExpenseId(expenseId);
        response.setApprovalLevel(approvalLevel);
        response.setStatus(null);
        response.setApproverUsername(approverUsername);
        response.setComments(comments);
        response.setAppliedRuleName(appliedRuleName);
        response.setCreatedAt(createdAt);

        assertEquals(id, response.getId());
        assertEquals(expenseId, response.getExpenseId());
        assertEquals(approvalLevel, response.getApprovalLevel());
        assertNull(response.getStatus());
        assertEquals(approverUsername, response.getApproverUsername());
        assertEquals(comments, response.getComments());
        assertEquals(appliedRuleName, response.getAppliedRuleName());
        assertEquals(createdAt, response.getCreatedAt());
    }
}