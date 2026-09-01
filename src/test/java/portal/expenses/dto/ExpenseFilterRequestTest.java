package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;
import portal.expenses.entity.ExpenseCategory;

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
    void testSettersAndGetters() {
        ExpenseFilterRequest request = new ExpenseFilterRequest();

        BigDecimal min = new BigDecimal("10.00");
        BigDecimal max = new BigDecimal("100.00");
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2023, 1, 31);

        request.setStatus(null);
        request.setCategory(null);
        request.setMinAmount(min);
        request.setMaxAmount(max);
        request.setStartDate(start);
        request.setEndDate(end);
        request.setHasReceipt(true);
        request.setPage(2);
        request.setSize(25);
        request.setSortBy("amount");
        request.setSortDirection("ASC");

        assertEquals(min, request.getMinAmount());
        assertEquals(max, request.getMaxAmount());
        assertEquals(start, request.getStartDate());
        assertEquals(end, request.getEndDate());
        assertTrue(request.getHasReceipt());
        assertEquals(2, request.getPage());
        assertEquals(25, request.getSize());
        assertEquals("amount", request.getSortBy());
        assertEquals("ASC", request.getSortDirection());
    }
}