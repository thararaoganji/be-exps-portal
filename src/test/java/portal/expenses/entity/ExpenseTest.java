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

        expense.setId(10L);
        assertEquals(10L, expense.getId());

        expense.setDescription("Dinner meeting");
        assertEquals("Dinner meeting", expense.getDescription());

        expense.setAmount(BigDecimal.valueOf(150.75));
        assertEquals(BigDecimal.valueOf(150.75), expense.getAmount());

        expense.setS3ObjectKey("receipts/rec123.png");
        assertEquals("receipts/rec123.png", expense.getS3ObjectKey());

        AppUser user = new AppUser();
        expense.setUser(user);
        assertEquals(user, expense.getUser());

        expense.setCategory(ExpenseCategory.OTHER);
        assertEquals(ExpenseCategory.OTHER, expense.getCategory());

        LocalDate date = LocalDate.of(2023, 5, 15);
        expense.setExpenseDate(date);
        assertEquals(date, expense.getExpenseDate());

        expense.setApprovalStatus(ApprovalStatus.PENDING);
        assertEquals(ApprovalStatus.PENDING, expense.getApprovalStatus());

        expense.setHasReceipt(false);
        assertFalse(expense.getHasReceipt());

        expense.setPolicyFlags("EXCEEDS_LIMIT");
        assertEquals("EXCEEDS_LIMIT", expense.getPolicyFlags());

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
    void testDefaults() {
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
        assertNull(expense.getExpenseDate());
        assertNull(expense.getCreatedAt());

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertNotNull(expense.getExpenseDate());
        assertEquals(LocalDate.now(), expense.getExpenseDate());
    }

    @Test
    void testOnCreateWhenExpenseDateIsAlreadySet() {
        Expense expense = new Expense();
        LocalDate specificDate = LocalDate.of(2022, 1, 1);
        expense.setExpenseDate(specificDate);

        expense.onCreate();

        assertNotNull(expense.getCreatedAt());
        assertEquals(specificDate, expense.getExpenseDate());
    }
}