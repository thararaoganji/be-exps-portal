package portal.expenses.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.nio.charset.StandardCharsets;
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

    @InjectMocks
    private FinanceController financeController;

    private Expense sampleExpense;

    @BeforeEach
    void setUp() {
        sampleExpense = new Expense();
    }

    @Test
    void testGetReimbursementQueue() {
        List<Expense> expectedExpenses = Collections.singletonList(sampleExpense);
        when(expenseService.getExpensesByStatus(ApprovalStatus.APPROVED)).thenReturn(expectedExpenses);

        ResponseEntity<List<Expense>> response = financeController.getReimbursementQueue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedExpenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.APPROVED);
    }

    @Test
    void testGetReimbursementQueuePaginated() {
        PageResponse<Expense> expectedPage = new PageResponse<>();
        when(expenseService.getExpensesByStatusWithPagination(ApprovalStatus.APPROVED, 0, 10, "createdAt", "DESC"))
                .thenReturn(expectedPage);

        ResponseEntity<PageResponse<Expense>> response = financeController.getReimbursementQueuePaginated(0, 10, "createdAt", "DESC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(expenseService).getExpensesByStatusWithPagination(ApprovalStatus.APPROVED, 0, 10, "createdAt", "DESC");
    }

    @Test
    void testGetReimbursedExpenses() {
        List<Expense> expectedExpenses = Collections.singletonList(sampleExpense);
        when(expenseService.getExpensesByStatus(ApprovalStatus.REIMBURSED)).thenReturn(expectedExpenses);

        ResponseEntity<List<Expense>> response = financeController.getReimbursedExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedExpenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.REIMBURSED);
    }

    @Test
    void testGetReimbursedExpensesPaginated() {
        PageResponse<Expense> expectedPage = new PageResponse<>();
        when(expenseService.getExpensesByStatusWithPagination(ApprovalStatus.REIMBURSED, 0, 10, "reimbursedAt", "DESC"))
                .thenReturn(expectedPage);

        ResponseEntity<PageResponse<Expense>> response = financeController.getReimbursedExpensesPaginated(0, 10, "reimbursedAt", "DESC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(expenseService).getExpensesByStatusWithPagination(ApprovalStatus.REIMBURSED, 0, 10, "reimbursedAt", "DESC");
    }

    @Test
    void testMarkAsReimbursed() {
        Long expenseId = 1L;
        when(financeService.markAsReimbursed(expenseId)).thenReturn(sampleExpense);

        ResponseEntity<Expense> response = financeController.markAsReimbursed(expenseId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleExpense, response.getBody());
        verify(financeService).markAsReimbursed(expenseId);
    }

    @Test
    void testExportReimbursedExpenses() {
        String startDate = "2023-01-01";
        String endDate = "2023-01-31";
        byte[] csvData = "id,amount,status\n1,100,REIMBURSED".getBytes(StandardCharsets.UTF_8);
        when(financeService.exportReimbursedExpensesToCsv(startDate, endDate)).thenReturn(csvData);

        ResponseEntity<Resource> response = financeController.exportReimbursedExpenses(startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=\"reimbursed_expenses.csv\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.parseMediaType("text/csv"), response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        verify(financeService).exportReimbursedExpensesToCsv(startDate, endDate);
    }

    @Test
    void testGetApprovedExpenses() {
        List<Expense> expectedExpenses = Collections.singletonList(sampleExpense);
        when(expenseService.getExpensesByStatus(ApprovalStatus.APPROVED)).thenReturn(expectedExpenses);

        ResponseEntity<List<Expense>> response = financeController.getApprovedExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedExpenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.APPROVED);
    }
}