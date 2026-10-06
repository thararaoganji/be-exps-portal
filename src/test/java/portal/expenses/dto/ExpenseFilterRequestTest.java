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

        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        BigDecimal minAmount = new BigDecimal("10.50");
        BigDecimal maxAmount = new BigDecimal("99.99");

        request.setStatus(null);
        request.setCategory(null);
        request.setMinAmount(minAmount);
        request.setMaxAmount(maxAmount);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setHasReceipt(true);
        request.setPage(2);
        request.setSize(25);
        request.setSortBy("amount");
        request.setSortDirection("ASC");

        assertNull(request.getStatus());
        assertNull(request.getCategory());
        assertEquals(minAmount, request.getMinAmount());
        assertEquals(maxAmount, request.getMaxAmount());
        assertEquals(startDate, request.getStartDate());
        assertEquals(endDate, request.getEndDate());
        assertTrue(request.getHasReceipt());
        assertEquals(2, request.getPage());
        assertEquals(25, request.getSize());
        assertEquals("amount", request.getSortBy());
        assertEquals("ASC", request.getSortDirection());
    }
}