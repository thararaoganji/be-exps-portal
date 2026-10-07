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

        expense.setId(100L);
        assertEquals(100L, expense.getId());

        expense.setDescription("Team lunch");
        assertEquals("Team lunch", expense.getDescription());

        BigDecimal amount = new BigDecimal("45.50");
        expense.setAmount(amount);
        assertEquals(amount, expense.getAmount());

        expense.setS3ObjectKey("receipts/receipt1.png");
        assertEquals("receipts/receipt1.png", expense.getS3ObjectKey());

        AppUser user = new AppUser();
        expense.setUser(user);
        assertSame(user, expense.getUser());

        expense.setCategory(ExpenseCategory.OTHER);
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());

        LocalDate date = LocalDate.of(2025, 5, 10);
        expense.setExpenseDate(date);
        assertEquals(date, expense.getExpenseDate());

        expense.setApprovalStatus(ApprovalStatus.PENDING);
        assertEquals(ApprovalStatus.PENDING, expense.getApprovalStatus());

        expense.setHasReceipt(true);
        assertTrue(expense.getHasReceipt());

        expense.setPolicyFlags("EXCEEDS_LIMIT");
        assertEquals("EXCEEDS_LIMIT", expense.getPolicyFlags());

        LocalDateTime now = LocalDateTime.now();
        expense.setSubmittedAt(now);
        assertEquals(now, expense.getSubmittedAt());

        expense.setApprovedAt(now.plusHours(1));
        assertEquals(now.plusHours(1), expense.getApprovedAt());

        expense.setRejectedAt(now.plusHours(2));
        assertEquals(now.plusHours(2), expense.getRejectedAt());

        expense.setReimbursedAt(now.plusHours(3));
        assertEquals(now.plusHours(3), expense.getReimbursedAt());
    }

    @Test
    void testDefaultValues() {
        Expense expense = new Expense();
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());
        assertEquals(ApprovalStatus.PENDING, expense.getApprovalStatus());
        assertTrue(expense.getHasReceipt());
        assertNull(expense.getCreatedAt());
    }

    @Test
    void testOnCreateWhenExpenseDateIsNull() {
        Expense expense = new Expense();
        assertNull(expense.getCreatedAt());
        assertNull(expense.getExpenseDate());

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertNotNull(expense.getExpenseDate());
    }

    @Test
    void testOnCreateWhenExpenseDateIsAlreadySet() {
        Expense expense = new Expense();
        LocalDate presetDate = LocalDate.of(2024, 1, 15);
        expense.setExpenseDate(presetDate);

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertEquals(presetDate, expense.getExpenseDate());
    }
}