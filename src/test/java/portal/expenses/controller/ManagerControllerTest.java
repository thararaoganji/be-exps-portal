package portal.expenses.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import portal.expenses.dto.ApprovalRequest;
import portal.expenses.dto.PageResponse;
import portal.expenses.entity.ApprovalStatus;
import portal.expenses.entity.Expense;
import portal.expenses.service.ApprovalService;
import portal.expenses.service.ExpenseService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private ApprovalService approvalService;

    private ManagerController managerController;

    @BeforeEach
    void setUp() {
        managerController = new ManagerController(expenseService, approvalService);
    }

    @Test
    void testGetApprovalQueue() {
        Expense expense = mock(Expense.class);
        List<Expense> expenseList = Collections.singletonList(expense);

        when(expenseService.getExpensesByStatus(ApprovalStatus.MANAGER_REVIEW)).thenReturn(expenseList);

        ResponseEntity<List<Expense>> response = managerController.getApprovalQueue();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenseList, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.MANAGER_REVIEW);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetApprovalQueuePaginated() {
        PageResponse<Expense> pageResponse = mock(PageResponse.class);
        int page = 0;
        int size = 10;
        String sortBy = "createdAt";
        String sortDirection = "DESC";

        when(expenseService.getExpensesByStatusWithPagination(
                ApprovalStatus.MANAGER_REVIEW, page, size, sortBy, sortDirection))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<Expense>> response = managerController.getApprovalQueuePaginated(
                page, size, sortBy, sortDirection);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
        verify(expenseService).getExpensesByStatusWithPagination(
                ApprovalStatus.MANAGER_REVIEW, page, size, sortBy, sortDirection);
    }

    @Test
    void testProcessApproval() {
        Long expenseId = 1L;
        String username = "managerUser";
        ApprovalRequest approvalRequest = mock(ApprovalRequest.class);
        Authentication authentication = mock(Authentication.class);
        Expense approvedExpense = mock(Expense.class);

        when(authentication.getName()).thenReturn(username);
        when(approvalRequest.getStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(approvalRequest.getComments()).thenReturn("Approved by manager");
        when(approvalService.processApproval(expenseId, username, ApprovalStatus.APPROVED, "Approved by manager"))
                .thenReturn(approvedExpense);

        ResponseEntity<Expense> response = managerController.processApproval(
                expenseId, approvalRequest, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(approvedExpense, response.getBody());
        verify(approvalService).processApproval(expenseId, username, ApprovalStatus.APPROVED, "Approved by manager");
    }
}