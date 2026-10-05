package portal.expenses.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private ApprovalService approvalService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ManagerController managerController;

    @Test
    void testGetApprovalQueue() {
        Expense expense = mock(Expense.class);
        List<Expense> expenses = List.of(expense);
        when(expenseService.getExpensesByStatus(ApprovalStatus.MANAGER_REVIEW)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = managerController.getApprovalQueue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.MANAGER_REVIEW);
    }

    @Test
    void testGetApprovalQueuePaginated() {
        PageResponse<Expense> pageResponse = mock(PageResponse.class);
        when(expenseService.getExpensesByStatusWithPagination(ApprovalStatus.MANAGER_REVIEW, 0, 10, "createdAt", "DESC"))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<Expense>> response = managerController.getApprovalQueuePaginated(0, 10, "createdAt", "DESC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
        verify(expenseService).getExpensesByStatusWithPagination(ApprovalStatus.MANAGER_REVIEW, 0, 10, "createdAt", "DESC");
    }

    @Test
    void testProcessApproval() {
        Long expenseId = 1L;
        ApprovalRequest approvalRequest = mock(ApprovalRequest.class);
        ApprovalStatus status = ApprovalStatus.MANAGER_REVIEW;
        String comments = "Approved by manager";
        String username = "manager1";

        when(authentication.getName()).thenReturn(username);
        when(approvalRequest.getStatus()).thenReturn(status);
        when(approvalRequest.getComments()).thenReturn(comments);

        Expense approvedExpense = mock(Expense.class);
        when(approvalService.processApproval(expenseId, username, status, comments)).thenReturn(approvedExpense);

        ResponseEntity<Expense> response = managerController.processApproval(expenseId, approvalRequest, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(approvedExpense, response.getBody());
        verify(approvalService).processApproval(expenseId, username, status, comments);
    }
}