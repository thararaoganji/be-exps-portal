package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {

    @Test
    void testGettersAndSetters() {
        Expense expense = new Expense();

        // Check defaults
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());
        assertEquals(ApprovalStatus.PENDING, expense.getApprovalStatus());
        assertEquals(Boolean.TRUE, expense.getHasReceipt());

        Long id = 100L;
        expense.setId(id);
        assertEquals(id, expense.getId());

        String description = "Travel ticket";
        expense.setDescription(description);
        assertEquals(description, expense.getDescription());

        BigDecimal amount = new BigDecimal("123.45");
        expense.setAmount(amount);
        assertEquals(amount, expense.getAmount());

        String s3Key = "receipts/100/doc.pdf";
        expense.setS3ObjectKey(s3Key);
        assertEquals(s3Key, expense.getS3ObjectKey());

        AppUser user = new AppUser();
        expense.setUser(user);
        assertSame(user, expense.getUser());

        expense.setCategory(ExpenseCategory.OTHER);
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());

        LocalDate expenseDate = LocalDate.of(2025, 1, 15);
        expense.setExpenseDate(expenseDate);
        assertEquals(expenseDate, expense.getExpenseDate());

        expense.setApprovalStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, expense.getApprovalStatus());

        expense.setHasReceipt(false);
        assertFalse(expense.getHasReceipt());

        String policyFlags = "FLAG_EXCEEDED_LIMIT";
        expense.setPolicyFlags(policyFlags);
        assertEquals(policyFlags, expense.getPolicyFlags());

        LocalDateTime now = LocalDateTime.now();
        expense.setSubmittedAt(now);
        assertEquals(now, expense.getSubmittedAt());

        LocalDateTime approvedAt = now.plusDays(1);
        expense.setApprovedAt(approvedAt);
        assertEquals(approvedAt, expense.getApprovedAt());

        LocalDateTime rejectedAt = now.plusDays(2);
        expense.setRejectedAt(rejectedAt);
        assertEquals(rejectedAt, expense.getRejectedAt());

        LocalDateTime reimbursedAt = now.plusDays(3);
        expense.setReimbursedAt(reimbursedAt);
        assertEquals(reimbursedAt, expense.getReimbursedAt());
    }

    @Test
    void testOnCreateWhenExpenseDateIsNull() {
        Expense expense = new Expense();
        assertNull(expense.getCreatedAt());
        assertNull(expense.getExpenseDate());

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertNotNull(expense.getExpenseDate());
        assertEquals(LocalDate.now(), expense.getExpenseDate());
    }

    @Test
    void testOnCreateWhenExpenseDateIsPreset() {
        Expense expense = new Expense();
        LocalDate presetDate = LocalDate.of(2024, 12, 1);
        expense.setExpenseDate(presetDate);

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertEquals(presetDate, expense.getExpenseDate());
    }
}