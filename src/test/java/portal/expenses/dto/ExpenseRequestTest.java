package portal.expenses.dto;

import org.junit.jupiter.api.Test;

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

        String description = "Team Lunch";
        BigDecimal amount = new BigDecimal("45.50");
        LocalDate date = LocalDate.of(2023, 5, 20);

        request.setDescription(description);
        request.setAmount(amount);
        request.setCategory(null);
        request.setExpenseDate(date);

        assertEquals(description, request.getDescription());
        assertEquals(amount, request.getAmount());
        assertNull(request.getCategory());
        assertEquals(date, request.getExpenseDate());
    }
}