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
    void testDefaultsAndSettersGetters() {
        ExpenseFilterRequest request = new ExpenseFilterRequest();

        // Check defaults
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

        // Set values
        ApprovalStatus status = null;
        ExpenseCategory category = null;
        BigDecimal minAmount = new BigDecimal("10.50");
        BigDecimal maxAmount = new BigDecimal("100.00");
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);
        Boolean hasReceipt = Boolean.TRUE;

        request.setStatus(status);
        request.setCategory(category);
        request.setMinAmount(minAmount);
        request.setMaxAmount(maxAmount);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setHasReceipt(hasReceipt);
        request.setPage(2);
        request.setSize(25);
        request.setSortBy("amount");
        request.setSortDirection("ASC");

        // Verify values
        assertEquals(status, request.getStatus());
        assertEquals(category, request.getCategory());
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