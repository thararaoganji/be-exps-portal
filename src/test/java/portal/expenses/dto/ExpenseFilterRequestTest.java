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

        // Set values and verify
        ApprovalStatus status = ApprovalStatus.values().length > 0 ? ApprovalStatus.values()[0] : null;
        ExpenseCategory category = ExpenseCategory.values().length > 0 ? ExpenseCategory.values()[0] : null;
        BigDecimal minAmount = new BigDecimal("10.50");
        BigDecimal maxAmount = new BigDecimal("100.00");
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        Boolean hasReceipt = Boolean.TRUE;
        Integer page = 2;
        Integer size = 25;
        String sortBy = "amount";
        String sortDirection = "ASC";

        request.setStatus(status);
        request.setCategory(category);
        request.setMinAmount(minAmount);
        request.setMaxAmount(maxAmount);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setHasReceipt(hasReceipt);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        assertEquals(status, request.getStatus());
        assertEquals(category, request.getCategory());
        assertEquals(minAmount, request.getMinAmount());
        assertEquals(maxAmount, request.getMaxAmount());
        assertEquals(startDate, request.getStartDate());
        assertEquals(endDate, request.getEndDate());
        assertTrue(request.getHasReceipt());
        assertEquals(page, request.getPage());
        assertEquals(size, request.getSize());
        assertEquals(sortBy, request.getSortBy());
        assertEquals(sortDirection, request.getSortDirection());
    }
}