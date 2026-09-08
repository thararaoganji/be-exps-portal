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

        Long id = 100L;
        String description = "Business Lunch";
        BigDecimal amount = new BigDecimal("45.50");
        String s3Key = "receipts/lunch.jpg";
        AppUser user = new AppUser();
        ExpenseCategory category = ExpenseCategory.OTHER;
        LocalDate expenseDate = LocalDate.of(2023, 5, 10);
        ApprovalStatus status = ApprovalStatus.PENDING;
        Boolean hasReceipt = true;
        String policyFlags = "FLAG_WEEKEND";
        LocalDateTime submittedAt = LocalDateTime.now().minusDays(2);
        LocalDateTime approvedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime rejectedAt = LocalDateTime.now().minusHours(12);
        LocalDateTime reimbursedAt = LocalDateTime.now().minusHours(6);

        expense.setId(id);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setS3ObjectKey(s3Key);
        expense.setUser(user);
        expense.setCategory(category);
        expense.setExpenseDate(expenseDate);
        expense.setApprovalStatus(status);
        expense.setHasReceipt(hasReceipt);
        expense.setPolicyFlags(policyFlags);
        expense.setSubmittedAt(submittedAt);
        expense.setApprovedAt(approvedAt);
        expense.setRejectedAt(rejectedAt);
        expense.setReimbursedAt(reimbursedAt);

        assertEquals(id, expense.getId());
        assertEquals(description, expense.getDescription());
        assertEquals(amount, expense.getAmount());
        assertEquals(s3Key, expense.getS3ObjectKey());
        assertEquals(user, expense.getUser());
        assertEquals(category, expense.getCategory());
        assertEquals(expenseDate, expense.getExpenseDate());
        assertEquals(status, expense.getApprovalStatus());
        assertEquals(hasReceipt, expense.getHasReceipt());
        assertEquals(policyFlags, expense.getPolicyFlags());
        assertEquals(submittedAt, expense.getSubmittedAt());
        assertEquals(approvedAt, expense.getApprovedAt());
        assertEquals(rejectedAt, expense.getRejectedAt());
        assertEquals(reimbursedAt, expense.getReimbursedAt());
        assertNull(expense.getCreatedAt());
    }

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
        assertNull(expense.getCreatedAt());
        assertNull(expense.getPolicyFlags());
        assertNull(expense.getSubmittedAt());
        assertNull(expense.getApprovedAt());
        assertNull(expense.getRejectedAt());
        assertNull(expense.getReimbursedAt());
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
    void testOnCreateWhenExpenseDateIsNotNull() {
        Expense expense = new Expense();
        LocalDate presetDate = LocalDate.of(2022, 1, 15);
        expense.setExpenseDate(presetDate);

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertEquals(presetDate, expense.getExpenseDate());
    }
}