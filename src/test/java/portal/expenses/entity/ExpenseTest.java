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

        expense.setDescription("Team Lunch");
        assertEquals("Team Lunch", expense.getDescription());

        expense.setAmount(new BigDecimal("45.50"));
        assertEquals(new BigDecimal("45.50"), expense.getAmount());

        expense.setS3ObjectKey("receipts/receipt-123.pdf");
        assertEquals("receipts/receipt-123.pdf", expense.getS3ObjectKey());

        AppUser user = new AppUser();
        expense.setUser(user);
        assertSame(user, expense.getUser());

        expense.setCategory(ExpenseCategory.OTHER);
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());

        LocalDate now = LocalDate.now();
        expense.setExpenseDate(now);
        assertEquals(now, expense.getExpenseDate());

        expense.setApprovalStatus(ApprovalStatus.PENDING);
        assertEquals(ApprovalStatus.PENDING, expense.getApprovalStatus());

        expense.setHasReceipt(false);
        assertFalse(expense.getHasReceipt());

        expense.setPolicyFlags("FLAG_OVER_LIMIT");
        assertEquals("FLAG_OVER_LIMIT", expense.getPolicyFlags());

        LocalDateTime time = LocalDateTime.now();
        expense.setSubmittedAt(time);
        assertEquals(time, expense.getSubmittedAt());

        expense.setApprovedAt(time.plusHours(1));
        assertEquals(time.plusHours(1), expense.getApprovedAt());

        expense.setRejectedAt(time.plusHours(2));
        assertEquals(time.plusHours(2), expense.getRejectedAt());

        expense.setReimbursedAt(time.plusHours(3));
        assertEquals(time.plusHours(3), expense.getReimbursedAt());
    }

    @Test
    void testDefaults() {
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
    void testOnCreateWhenExpenseDateIsNotNull() {
        Expense expense = new Expense();
        LocalDate specificDate = LocalDate.of(2025, 1, 15);
        expense.setExpenseDate(specificDate);

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertEquals(specificDate, expense.getExpenseDate());
    }
}