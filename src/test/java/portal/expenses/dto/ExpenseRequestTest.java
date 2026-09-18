package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExpenseRequestTest {

    @Test
    void testGettersAndSetters() {
        ExpenseRequest request = new ExpenseRequest();

        assertNull(request.getDescription());
        assertNull(request.getAmount());
        assertNull(request.getCategory());
        assertNull(request.getExpenseDate());

        String description = "Office Supplies";
        BigDecimal amount = new BigDecimal("49.99");
        LocalDate expenseDate = LocalDate.of(2023, 10, 15);

        request.setDescription(description);
        request.setAmount(amount);
        request.setCategory(null);
        request.setExpenseDate(expenseDate);

        assertEquals(description, request.getDescription());
        assertEquals(amount, request.getAmount());
        assertNull(request.getCategory());
        assertEquals(expenseDate, request.getExpenseDate());
    }
}