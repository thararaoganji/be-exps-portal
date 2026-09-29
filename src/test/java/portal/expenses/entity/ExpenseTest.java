package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ExpenseTest {

    @Test
    void testGettersAndSetters() {
        Expense expense = new Expense();

        // Defaults
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());
        assertEquals(ApprovalStatus.PENDING, expense.getApprovalStatus());
        assertTrue(expense.getHasReceipt());

        // Setters and getters
        expense.setId(100L);
        assertEquals(100L, expense.getId());

        expense.setDescription("Lunch meeting");
        assertEquals("Lunch meeting", expense.getDescription());

        BigDecimal amount = new BigDecimal("45.50");
        expense.setAmount(amount);
        assertEquals(amount, expense.getAmount());

        expense.setS3ObjectKey("s3/path/receipt.pdf");
        assertEquals("s3/path/receipt.pdf", expense.getS3ObjectKey());

        AppUser user = mock(AppUser.class);
        expense.setUser(user);
        assertSame(user, expense.getUser());

        expense.setCategory(ExpenseCategory.OTHER);
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());

        LocalDate date = LocalDate.of(2025, 5, 20);
        expense.setExpenseDate(date);
        assertEquals(date, expense.getExpenseDate());

        expense.setApprovalStatus(ApprovalStatus.PENDING);
        assertEquals(ApprovalStatus.PENDING, expense.getApprovalStatus());

        expense.setHasReceipt(false);
        assertFalse(expense.getHasReceipt());

        expense.setPolicyFlags("EXCEEDS_LIMIT");
        assertEquals("EXCEEDS_LIMIT", expense.getPolicyFlags());

        LocalDateTime submitted = LocalDateTime.of(2025, 5, 20, 10, 0);
        expense.setSubmittedAt(submitted);
        assertEquals(submitted, expense.getSubmittedAt());

        LocalDateTime approved = LocalDateTime.of(2025, 5, 21, 11, 0);
        expense.setApprovedAt(approved);
        assertEquals(approved, expense.getApprovedAt());

        LocalDateTime rejected = LocalDateTime.of(2025, 5, 22, 12, 0);
        expense.setRejectedAt(rejected);
        assertEquals(rejected, expense.getRejectedAt());

        LocalDateTime reimbursed = LocalDateTime.of(2025, 5, 23, 13, 0);
        expense.setReimbursedAt(reimbursed);
        assertEquals(reimbursed, expense.getReimbursedAt());
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
        LocalDate presetDate = LocalDate.of(2024, 1, 1);
        expense.setExpenseDate(presetDate);

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertEquals(presetDate, expense.getExpenseDate());
    }
}