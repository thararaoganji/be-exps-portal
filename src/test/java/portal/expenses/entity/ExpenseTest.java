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

        expense.setDescription("Lunch meeting");
        assertEquals("Lunch meeting", expense.getDescription());

        BigDecimal amount = new BigDecimal("45.50");
        expense.setAmount(amount);
        assertEquals(amount, expense.getAmount());

        expense.setS3ObjectKey("receipts/receipt1.png");
        assertEquals("receipts/receipt1.png", expense.getS3ObjectKey());

        AppUser user = new AppUser();
        expense.setUser(user);
        assertEquals(user, expense.getUser());

        expense.setCategory(ExpenseCategory.MEALS);
        assertEquals(ExpenseCategory.MEALS, expense.getCategory());

        LocalDate expenseDate = LocalDate.of(2023, 10, 15);
        expense.setExpenseDate(expenseDate);
        assertEquals(expenseDate, expense.getExpenseDate());

        expense.setApprovalStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, expense.getApprovalStatus());

        expense.setHasReceipt(false);
        assertFalse(expense.getHasReceipt());

        expense.setPolicyFlags("EXCEEDS_DAILY_LIMIT");
        assertEquals("EXCEEDS_DAILY_LIMIT", expense.getPolicyFlags());

        LocalDateTime now = LocalDateTime.now();

        expense.setSubmittedAt(now.minusHours(3));
        assertEquals(now.minusHours(3), expense.getSubmittedAt());

        expense.setApprovedAt(now.minusHours(2));
        assertEquals(now.minusHours(2), expense.getApprovedAt());

        expense.setRejectedAt(now.minusHours(1));
        assertEquals(now.minusHours(1), expense.getRejectedAt());

        expense.setReimbursedAt(now);
        assertEquals(now, expense.getReimbursedAt());
    }

    @Test
    void testDefaultValues() {
        Expense expense = new Expense();
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());
        assertEquals(ApprovalStatus.PENDING, expense.getApprovalStatus());
        assertTrue(expense.getHasReceipt());
        assertNull(expense.getCreatedAt());
        assertNull(expense.getExpenseDate());
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
    void testOnCreateWhenExpenseDateIsAlreadySet() {
        Expense expense = new Expense();
        LocalDate existingDate = LocalDate.of(2022, 5, 20);
        expense.setExpenseDate(existingDate);

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertEquals(existingDate, expense.getExpenseDate());
    }
}