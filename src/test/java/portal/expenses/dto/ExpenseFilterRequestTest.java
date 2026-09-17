package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpenseFilterRequestTest {

    @Test
    void testDefaultValues() {
        ExpenseFilterRequest request = new ExpenseFilterRequest();

        assertNull(request.getStatus());
        assertNull(request.getCategory());
        assertNull(request.getMinAmount());
        assertNull(request.getMaxAmount());
        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
        assertNull(request.getHasReceipt());
        assertEquals(0, request.getPage());
        assertEquals(10, request.getSize());
        assertEquals("createdAt", request.getSortBy());
        assertEquals("DESC", request.getSortDirection());
    }

    @Test
    void testGettersAndSetters() {
        ExpenseFilterRequest request = new ExpenseFilterRequest();

        request.setStatus(null);
        request.setCategory(null);

        BigDecimal min = new BigDecimal("10.50");
        request.setMinAmount(min);
        assertEquals(min, request.getMinAmount());

        BigDecimal max = new BigDecimal("100.00");
        request.setMaxAmount(max);
        assertEquals(max, request.getMaxAmount());

        LocalDate start = LocalDate.of(2023, 1, 1);
        request.setStartDate(start);
        assertEquals(start, request.getStartDate());

        LocalDate end = LocalDate.of(2023, 1, 31);
        request.setEndDate(end);
        assertEquals(end, request.getEndDate());

        request.setHasReceipt(true);
        assertTrue(request.getHasReceipt());

        request.setPage(2);
        assertEquals(2, request.getPage());

        request.setSize(25);
        assertEquals(25, request.getSize());

        request.setSortBy("amount");
        assertEquals("amount", request.getSortBy());

        request.setSortDirection("ASC");
        assertEquals("ASC", request.getSortDirection());
    }
}