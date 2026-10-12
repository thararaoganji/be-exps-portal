package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {

    @Test
    void testDefaults() {
        Expense expense = new Expense();

        assertEquals(ExpenseCategory.OTHER, expense.getCategory());
        assertEquals(ApprovalStatus.PENDING, expense.getApprovalStatus());
        assertTrue(expense.getHasReceipt());
        assertNull(expense.getId());
        assertNull(expense.getDescription());
        assertNull(expense.getAmount());
        assertNull(expense.getS3ObjectKey());
        assertNull(expense.getUser());
        assertNull(expense.getExpenseDate());
        assertNull(expense.getPolicyFlags());
        assertNull(expense.getCreatedAt());
        assertNull(expense.getSubmittedAt());
        assertNull(expense.getApprovedAt());
        assertNull(expense.getRejectedAt());
        assertNull(expense.getReimbursedAt());
    }

    @Test
    void testGettersAndSetters() {
        Expense expense = new Expense();
        AppUser user = new AppUser();

        Long id = 42L;
        String description = "Lunch with client";
        BigDecimal amount = new BigDecimal("125.50");
        String s3Key = "receipts/doc123.pdf";
        LocalDate date = LocalDate.of(2025, 5, 20);
        String policyFlags = "FLAG_WEEKEND";
        LocalDateTime now = LocalDateTime.of(2025, 5, 20, 12, 0);
        LocalDateTime submittedAt = now.plusHours(1);
        LocalDateTime approvedAt = now.plusHours(2);
        LocalDateTime rejectedAt = now.plusHours(3);
        LocalDateTime reimbursedAt = now.plusHours(4);

        expense.setId(id);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setS3ObjectKey(s3Key);
        expense.setUser(user);
        expense.setCategory(ExpenseCategory.OTHER);
        expense.setExpenseDate(date);
        expense.setApprovalStatus(ApprovalStatus.APPROVED);
        expense.setHasReceipt(false);
        expense.setPolicyFlags(policyFlags);
        expense.setSubmittedAt(submittedAt);
        expense.setApprovedAt(approvedAt);
        expense.setRejectedAt(rejectedAt);
        expense.setReimbursedAt(reimbursedAt);

        assertEquals(id, expense.getId());
        assertEquals(description, expense.getDescription());
        assertEquals(amount, expense.getAmount());
        assertEquals(s3Key, expense.getS3ObjectKey());
        assertSame(user, expense.getUser());
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());
        assertEquals(date, expense.getExpenseDate());
        assertEquals(ApprovalStatus.APPROVED, expense.getApprovalStatus());
        assertFalse(expense.getHasReceipt());
        assertEquals(policyFlags, expense.getPolicyFlags());
        assertEquals(submittedAt, expense.getSubmittedAt());
        assertEquals(approvedAt, expense.getApprovedAt());
        assertEquals(rejectedAt, expense.getRejectedAt());
        assertEquals(reimbursedAt, expense.getReimbursedAt());
    }

    @Test
    void testOnCreateWithoutExpenseDate() {
        Expense expense = new Expense();

        assertNull(expense.getCreatedAt());
        assertNull(expense.getExpenseDate());

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertNotNull(expense.getExpenseDate());
        assertEquals(LocalDate.now(), expense.getExpenseDate());
    }

    @Test
    void testOnCreateWithExistingExpenseDate() {
        Expense expense = new Expense();
        LocalDate existingDate = LocalDate.of(2024, 1, 15);
        expense.setExpenseDate(existingDate);

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertEquals(existingDate, expense.getExpenseDate());
    }
}