package portal.expenses.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
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
import static org.mockito.Mockito.mock;
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
        Expense expense = mock(Expense.class);
        List<Expense> expenses = Collections.singletonList(expense);
        when(expenseService.getExpensesByStatus(ApprovalStatus.APPROVED)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = financeController.getReimbursementQueue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.APPROVED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetReimbursementQueuePaginated() {
        PageResponse<Expense> pageResponse = mock(PageResponse.class);
        when(expenseService.getExpensesByStatusWithPagination(ApprovalStatus.APPROVED, 0, 10, "amount", "DESC"))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<Expense>> response = financeController.getReimbursementQueuePaginated(0, 10, "amount", "DESC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
        verify(expenseService).getExpensesByStatusWithPagination(ApprovalStatus.APPROVED, 0, 10, "amount", "DESC");
    }

    @Test
    void testGetReimbursedExpenses() {
        Expense expense = mock(Expense.class);
        List<Expense> expenses = Collections.singletonList(expense);
        when(expenseService.getExpensesByStatus(ApprovalStatus.REIMBURSED)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = financeController.getReimbursedExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.REIMBURSED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetReimbursedExpensesPaginated() {
        PageResponse<Expense> pageResponse = mock(PageResponse.class);
        when(expenseService.getExpensesByStatusWithPagination(ApprovalStatus.REIMBURSED, 1, 20, "reimbursedAt", "ASC"))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<Expense>> response = financeController.getReimbursedExpensesPaginated(1, 20, "reimbursedAt", "ASC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
        verify(expenseService).getExpensesByStatusWithPagination(ApprovalStatus.REIMBURSED, 1, 20, "reimbursedAt", "ASC");
    }

    @Test
    void testMarkAsReimbursed() {
        Expense expense = mock(Expense.class);
        when(financeService.markAsReimbursed(123L)).thenReturn(expense);

        ResponseEntity<Expense> response = financeController.markAsReimbursed(123L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expense, response.getBody());
        verify(financeService).markAsReimbursed(123L);
    }

    @Test
    void testExportReimbursedExpenses() {
        byte[] csvData = "id,amount\n1,100".getBytes();
        when(financeService.exportReimbursedExpensesToCsv("2023-01-01", "2023-01-31")).thenReturn(csvData);

        ResponseEntity<Resource> response = financeController.exportReimbursedExpenses("2023-01-01", "2023-01-31");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentDisposition());
        assertEquals("attachment; filename=\"reimbursed_expenses.csv\"", response.getHeaders().getFirst("Content-Disposition"));
        assertEquals(MediaType.parseMediaType("text/csv"), response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        verify(financeService).exportReimbursedExpensesToCsv("2023-01-01", "2023-01-31");
    }

    @Test
    void testGetApprovedExpenses() {
        Expense expense = mock(Expense.class);
        List<Expense> expenses = Collections.singletonList(expense);
        when(expenseService.getExpensesByStatus(ApprovalStatus.APPROVED)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = financeController.getApprovedExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.APPROVED);
    }
}