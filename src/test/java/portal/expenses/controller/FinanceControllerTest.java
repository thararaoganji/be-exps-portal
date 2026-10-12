package portal.expenses.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import portal.expenses.dto.PageResponse;
import portal.expenses.entity.ApprovalStatus;
import portal.expenses.entity.Expense;
import portal.expenses.service.ExpenseService;
import portal.expenses.service.FinanceService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceControllerTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private FinanceService financeService;

    private FinanceController financeController;

    @BeforeEach
    void setUp() {
        financeController = new FinanceController(expenseService, financeService);
    }

    @Test
    void testGetReimbursementQueue() {
        Expense expense = new Expense();
        List<Expense> expenses = Collections.singletonList(expense);
        when(expenseService.getExpensesByStatus(ApprovalStatus.APPROVED)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = financeController.getReimbursementQueue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.APPROVED);
    }

    @Test
    void testGetReimbursementQueuePaginated() {
        @SuppressWarnings("unchecked")
        PageResponse<Expense> pageResponse = new PageResponse<>();
        when(expenseService.getExpensesByStatusWithPagination(ApprovalStatus.APPROVED, 0, 10, "createdAt", "DESC"))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<Expense>> response = financeController.getReimbursementQueuePaginated(
                0, 10, "createdAt", "DESC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
        verify(expenseService).getExpensesByStatusWithPagination(ApprovalStatus.APPROVED, 0, 10, "createdAt", "DESC");
    }

    @Test
    void testGetReimbursedExpenses() {
        Expense expense = new Expense();
        List<Expense> expenses = Collections.singletonList(expense);
        when(expenseService.getExpensesByStatus(ApprovalStatus.REIMBURSED)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = financeController.getReimbursedExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.REIMBURSED);
    }

    @Test
    void testGetReimbursedExpensesPaginated() {
        @SuppressWarnings("unchecked")
        PageResponse<Expense> pageResponse = new PageResponse<>();
        when(expenseService.getExpensesByStatusWithPagination(ApprovalStatus.REIMBURSED, 1, 20, "reimbursedAt", "ASC"))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<Expense>> response = financeController.getReimbursedExpensesPaginated(
                1, 20, "reimbursedAt", "ASC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
        verify(expenseService).getExpensesByStatusWithPagination(ApprovalStatus.REIMBURSED, 1, 20, "reimbursedAt", "ASC");
    }

    @Test
    void testMarkAsReimbursed() {
        Long expenseId = 42L;
        Expense expense = new Expense();
        when(financeService.markAsReimbursed(expenseId)).thenReturn(expense);

        ResponseEntity<Expense> response = financeController.markAsReimbursed(expenseId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expense, response.getBody());
        verify(financeService).markAsReimbursed(expenseId);
    }

    @Test
    void testExportReimbursedExpenses() {
        String startDate = "2023-01-01";
        String endDate = "2023-01-31";
        byte[] csvBytes = "id,amount\n1,100".getBytes();
        when(financeService.exportReimbursedExpensesToCsv(startDate, endDate)).thenReturn(csvBytes);

        ResponseEntity<Resource> response = financeController.exportReimbursedExpenses(startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=\"reimbursed_expenses.csv\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.parseMediaType("text/csv"), response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertArrayEquals(csvBytes, ((ByteArrayResource) response.getBody()).getByteArray());
        verify(financeService).exportReimbursedExpensesToCsv(startDate, endDate);
    }

    @Test
    void testGetApprovedExpenses() {
        Expense expense = new Expense();
        List<Expense> expenses = Collections.singletonList(expense);
        when(expenseService.getExpensesByStatus(ApprovalStatus.APPROVED)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = financeController.getApprovedExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.APPROVED);
    }
}