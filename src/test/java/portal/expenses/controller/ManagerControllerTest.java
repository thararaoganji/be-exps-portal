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
        List<Expense> mockExpenses = Collections.emptyList();
        when(expenseService.getExpensesByStatus(ApprovalStatus.MANAGER_REVIEW)).thenReturn(mockExpenses);

        ResponseEntity<List<Expense>> response = managerController.getApprovalQueue();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockExpenses, response.getBody());
        verify(expenseService).getExpensesByStatus(ApprovalStatus.MANAGER_REVIEW);
    }

    @Test
    void testGetApprovalQueuePaginated() {
        @SuppressWarnings("unchecked")
        PageResponse<Expense> mockPageResponse = mock(PageResponse.class);
        when(expenseService.getExpensesByStatusWithPagination(ApprovalStatus.MANAGER_REVIEW, 0, 10, "createdAt", "DESC"))
                .thenReturn(mockPageResponse);

        ResponseEntity<PageResponse<Expense>> response = managerController.getApprovalQueuePaginated(0, 10, "createdAt", "DESC");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPageResponse, response.getBody());
        verify(expenseService).getExpensesByStatusWithPagination(ApprovalStatus.MANAGER_REVIEW, 0, 10, "createdAt", "DESC");
    }

    @Test
    void testProcessApproval() {
        Long expenseId = 1L;
        String username = "testManager";
        ApprovalRequest approvalRequest = mock(ApprovalRequest.class);
        when(approvalRequest.getStatus()).thenReturn(ApprovalStatus.MANAGER_REVIEW);
        when(approvalRequest.getComments()).thenReturn("Approved by manager");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        Expense mockExpense = mock(Expense.class);
        when(approvalService.processApproval(expenseId, username, ApprovalStatus.MANAGER_REVIEW, "Approved by manager"))
                .thenReturn(mockExpense);

        ResponseEntity<Expense> response = managerController.processApproval(expenseId, approvalRequest, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockExpense, response.getBody());
        verify(approvalService).processApproval(expenseId, username, ApprovalStatus.MANAGER_REVIEW, "Approved by manager");
    }
}