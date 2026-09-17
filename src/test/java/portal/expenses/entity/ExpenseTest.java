package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpenseTest {

    @Test
    void testGettersAndSetters() {
        Expense expense = new Expense();

        Long id = 100L;
        String description = "Travel expenses";
        BigDecimal amount = new BigDecimal("150.75");
        String s3ObjectKey = "receipts/rec123.png";
        AppUser user = new AppUser();
        ExpenseCategory category = ExpenseCategory.TRAVEL;
        LocalDate expenseDate = LocalDate.of(2023, 10, 15);
        ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;
        Boolean hasReceipt = true;
        String policyFlags = "NO_FLAG";
        LocalDateTime submittedAt = LocalDateTime.of(2023, 10, 16, 10, 0);
        LocalDateTime approvedAt = LocalDateTime.of(2023, 10, 17, 11, 0);
        LocalDateTime rejectedAt = LocalDateTime.of(2023, 10, 18, 12, 0);
        LocalDateTime reimbursedAt = LocalDateTime.of(2023, 10, 19, 13, 0);

        expense.setId(id);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setS3ObjectKey(s3ObjectKey);
        expense.setUser(user);
        expense.setCategory(category);
        expense.setExpenseDate(expenseDate);
        expense.setApprovalStatus(approvalStatus);
        expense.setHasReceipt(hasReceipt);
        expense.setPolicyFlags(policyFlags);
        expense.setSubmittedAt(submittedAt);
        expense.setApprovedAt(approvedAt);
        expense.setRejectedAt(rejectedAt);
        expense.setReimbursedAt(reimbursedAt);

        assertEquals(id, expense.getId());
        assertEquals(description, expense.getDescription());
        assertEquals(amount, expense.getAmount());
        assertEquals(s3ObjectKey, expense.getS3ObjectKey());
        assertEquals(user, expense.getUser());
        assertEquals(category, expense.getCategory());
        assertEquals(expenseDate, expense.getExpenseDate());
        assertEquals(approvalStatus, expense.getApprovalStatus());
        assertEquals(hasReceipt, expense.getHasReceipt());
        assertEquals(policyFlags, expense.getPolicyFlags());
        assertEquals(submittedAt, expense.getSubmittedAt());
        assertEquals(approvedAt, expense.getApprovedAt());
        assertEquals(rejectedAt, expense.getRejectedAt());
        assertEquals(reimbursedAt, expense.getReimbursedAt());
    }

    @Test
    void testDefaultValues() {
        Expense expense = new Expense();
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());
        assertEquals(ApprovalStatus.PENDING, expense.getApprovalStatus());
        assertTrue(expense.getHasReceipt());
    }

    @Test
    void testOnCreateWhenExpenseDateIsNull() {
        Expense expense = new Expense();
        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertNotNull(expense.getExpenseDate());
        assertEquals(LocalDate.now(), expense.getExpenseDate());
    }

    @Test
    void testOnCreateWhenExpenseDateIsNotNull() {
        Expense expense = new Expense();
        LocalDate specificDate = LocalDate.of(2023, 5, 20);
        expense.setExpenseDate(specificDate);

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertEquals(specificDate, expense.getExpenseDate());
    }
}