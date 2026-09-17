package portal.expenses.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import portal.expenses.dto.ApprovalRequest;
import portal.expenses.dto.AuditLogResponse;
import portal.expenses.dto.ExpenseApprovalResponse;
import portal.expenses.dto.ExpenseDetailResponse;
import portal.expenses.dto.ExpenseFilterRequest;
import portal.expenses.dto.ExpenseRequest;
import portal.expenses.dto.PageResponse;
import portal.expenses.entity.ApprovalStatus;
import portal.expenses.entity.AuditEntry;
import portal.expenses.entity.Expense;
import portal.expenses.entity.ExpenseApproval;
import portal.expenses.service.ApprovalService;
import portal.expenses.service.ExpenseService;
import portal.expenses.service.LocalStorageService;
import portal.expenses.service.S3Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private S3Service s3Service;

    @Mock
    private LocalStorageService localStorageService;

    @Mock
    private ApprovalService approvalService;

    @Mock
    private Authentication authentication;

    private ExpenseController controller;

    @BeforeEach
    void setUp() {
        controller = new ExpenseController(expenseService, s3Service, localStorageService, approvalService);
        ReflectionTestUtils.setField(controller, "storageMode", "local");
    }

    @Test
    void testCreateExpense() throws IOException {
        when(authentication.getName()).thenReturn("testuser");

        ExpenseRequest request = mock(ExpenseRequest.class);
        when(request.getDescription()).thenReturn("Team Lunch");
        when(request.getAmount()).thenReturn(new BigDecimal("50.00"));
        when(request.getExpenseDate()).thenReturn(LocalDate.of(2025, 1, 1));

        MultipartFile receipt = new MockMultipartFile("receipt", "receipt.png", "image/png", new byte[]{1, 2, 3});

        Expense createdExpense = mock(Expense.class);
        when(createdExpense.getId()).thenReturn(1L);
        when(createdExpense.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);

        when(expenseService.createExpense(any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(createdExpense);

        ResponseEntity<Expense> response = controller.createExpense(request, receipt, false, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdExpense, response.getBody());
    }

    @Test
    void testGetMyExpenses() {
        when(authentication.getName()).thenReturn("testuser");
        Expense expense = mock(Expense.class);
        when(expenseService.getExpensesForCurrentUser("testuser")).thenReturn(List.of(expense));

        ResponseEntity<List<Expense>> response = controller.getMyExpenses(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(expense, response.getBody().get(0));
    }

    @Test
    void testGetMyExpensesWithFilters() {
        when(authentication.getName()).thenReturn("testuser");

        ExpenseFilterRequest filter = mock(ExpenseFilterRequest.class);
        when(filter.getPage()).thenReturn(0);
        when(filter.getSize()).thenReturn(10);
        when(filter.getSortBy()).thenReturn("createdAt");
        when(filter.getSortDirection()).thenReturn("DESC");

        Expense expense = mock(Expense.class);
        PageResponse<Expense> pageResponse = mock(PageResponse.class);
        when(pageResponse.getContent()).thenReturn(List.of(expense));
        when(pageResponse.getPageNumber()).thenReturn(0);
        when(pageResponse.getTotalPages()).thenReturn(1);

        when(expenseService.getExpensesWithFilters(eq("testuser"), any(ExpenseFilterRequest.class))).thenReturn(pageResponse);

        ResponseEntity<PageResponse<Expense>> response = controller.getMyExpensesWithFilters(filter, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
    }

    @Test
    void testGetPendingApprovals() {
        when(authentication.getName()).thenReturn("manager");
        Expense expense = mock(Expense.class);
        when(expenseService.getExpensesByStatus(ApprovalStatus.MANAGER_REVIEW)).thenReturn(List.of(expense));

        ResponseEntity<List<Expense>> response = controller.getPendingApprovals(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetApprovedExpenses() {
        Expense expense = mock(Expense.class);
        when(expenseService.getExpensesByStatus(ApprovalStatus.APPROVED)).thenReturn(List.of(expense));

        ResponseEntity<List<Expense>> response = controller.getApprovedExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetManagerQueue() {
        Expense expense = mock(Expense.class);
        when(expenseService.getExpensesByStatus(ApprovalStatus.MANAGER_REVIEW)).thenReturn(List.of(expense));

        ResponseEntity<List<Expense>> response = controller.getManagerQueue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetFinanceQueue() {
        Expense expense = mock(Expense.class);
        when(expenseService.getExpensesByStatus(ApprovalStatus.FINANCE_REVIEW)).thenReturn(List.of(expense));

        ResponseEntity<List<Expense>> response = controller.getFinanceQueue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetReimbursedExpenses() {
        Expense expense = mock(Expense.class);
        when(expenseService.getExpensesByStatus(ApprovalStatus.REIMBURSED)).thenReturn(List.of(expense));

        ResponseEntity<List<Expense>> response = controller.getReimbursedExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetExpense() {
        Expense expense = mock(Expense.class);
        when(expenseService.getExpenseById(10L)).thenReturn(expense);

        ResponseEntity<Expense> response = controller.getExpense(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expense, response.getBody());
    }

    @Test
    void testGetExpenseDetails() {
        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(10L);
        when(expenseService.getExpenseById(10L)).thenReturn(expense);

        ExpenseApproval approval = mock(ExpenseApproval.class);
        when(approval.getId()).thenReturn(100L);
        when(approval.getExpense()).thenReturn(expense);
        when(approval.getStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(approval.getComments()).thenReturn("Looks good");
        when(approval.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(approval.getApprover()).thenReturn(null);
        when(approval.getAppliedRule()).thenReturn(null);

        when(approvalService.getApprovalHistory(10L)).thenReturn(List.of(approval));

        ResponseEntity<ExpenseDetailResponse> response = controller.getExpenseDetails(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expense, response.getBody().getExpense());
        assertEquals(1, response.getBody().getApprovals().size());
        assertEquals(100L, response.getBody().getApprovals().get(0).getId());
    }

    @Test
    void testSubmitExpense() {
        when(authentication.getName()).thenReturn("testuser");
        Expense submitted = mock(Expense.class);
        when(submitted.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);
        when(expenseService.submitExpense(10L, "testuser")).thenReturn(submitted);

        ResponseEntity<Expense> response = controller.submitExpense(10L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(submitted, response.getBody());
    }

    @Test
    void testUpdateExpense() throws IOException {
        when(authentication.getName()).thenReturn("testuser");

        ExpenseRequest request = mock(ExpenseRequest.class);
        when(request.getDescription()).thenReturn("Updated Desc");
        when(request.getAmount()).thenReturn(new BigDecimal("75.00"));
        when(request.getExpenseDate()).thenReturn(LocalDate.of(2025, 2, 1));

        MultipartFile receipt = new MockMultipartFile("receipt", "test.pdf", "application/pdf", new byte[]{4, 5, 6});

        Expense updated = mock(Expense.class);
        when(updated.getApprovalStatus()).thenReturn(ApprovalStatus.DRAFT);

        when(expenseService.updateExpense(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(updated);

        ResponseEntity<Expense> response = controller.updateExpense(10L, request, receipt, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
    }

    @Test
    void testGetReceiptNotFoundWhenNoKey() {
        Expense expense = mock(Expense.class);
        when(expense.getS3ObjectKey()).thenReturn(null);
        when(expenseService.getExpenseById(10L)).thenReturn(expense);

        ResponseEntity<Resource> response = controller.getReceipt(10L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetReceiptLocalStorage() {
        Expense expense = mock(Expense.class);
        when(expense.getS3ObjectKey()).thenReturn("receipt-10.pdf");
        when(expenseService.getExpenseById(10L)).thenReturn(expense);
        when(localStorageService.downloadFile("receipt-10.pdf")).thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<Resource> response = controller.getReceipt(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetReceiptS3Storage() {
        ReflectionTestUtils.setField(controller, "storageMode", "s3");
        Expense expense = mock(Expense.class);
        when(expense.getS3ObjectKey()).thenReturn("receipt-10.pdf");
        when(expenseService.getExpenseById(10L)).thenReturn(expense);
        when(s3Service.downloadFile("receipt-10.pdf")).thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<Resource> response = controller.getReceipt(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testApproveExpenseSuccess() {
        when(authentication.getName()).thenReturn("manager");

        ApprovalRequest request = mock(ApprovalRequest.class);
        when(request.getStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(request.getComments()).thenReturn("Approved by manager");

        Expense approvedExpense = mock(Expense.class);
        when(approvedExpense.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);

        when(approvalService.processApproval(10L, "manager", ApprovalStatus.APPROVED, "Approved by manager"))
                .thenReturn(approvedExpense);

        ResponseEntity<Expense> response = controller.approveExpense(10L, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(approvedExpense, response.getBody());
    }

    @Test
    void testApproveExpenseThrowsWhenStatusIsNull() {
        when(authentication.getName()).thenReturn("manager");

        ApprovalRequest request = mock(ApprovalRequest.class);
        when(request.getStatus()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> controller.approveExpense(10L, request, authentication));
    }

    @Test
    void testGetApprovalHistory() {
        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(10L);

        ExpenseApproval approval = mock(ExpenseApproval.class);
        when(approval.getId()).thenReturn(200L);
        when(approval.getExpense()).thenReturn(expense);
        when(approval.getStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(approval.getComments()).thenReturn("OK");
        when(approval.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(approval.getApprover()).thenReturn(null);
        when(approval.getAppliedRule()).thenReturn(null);

        when(approvalService.getApprovalHistory(10L)).thenReturn(List.of(approval));

        ResponseEntity<List<ExpenseApprovalResponse>> response = controller.getApprovalHistory(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(200L, response.getBody().get(0).getId());
    }

    @Test
    void testGetAuditLog() {
        AuditEntry auditEntry = mock(AuditEntry.class);
        when(auditEntry.getId()).thenReturn(300L);
        when(auditEntry.getAction()).thenReturn("UPDATE");
        when(auditEntry.getOldStatus()).thenReturn("DRAFT");
        when(auditEntry.getNewStatus()).thenReturn("PENDING");
        when(auditEntry.getComment()).thenReturn("Submitted");
        when(auditEntry.getPerformedBy()).thenReturn("testuser");
        when(auditEntry.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(expenseService.getAuditLog(10L)).thenReturn(List.of(auditEntry));

        ResponseEntity<List<AuditLogResponse>> response = controller.getAuditLog(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        AuditLogResponse dto = response.getBody().get(0);
        assertEquals(300L, dto.getId());
        assertEquals("UPDATE", dto.getAction());
        assertEquals("DRAFT", dto.getOldStatus());
        assertEquals("PENDING", dto.getNewStatus());
        assertEquals("Submitted", dto.getComment());
        assertEquals("testuser", dto.getPerformedBy());
        assertNotNull(dto.getCreatedAt());
    }

    @Test
    void testMarkAsReimbursed() {
        when(authentication.getName()).thenReturn("finance");
        Expense reimbursed = mock(Expense.class);
        when(expenseService.markAsReimbursed(10L)).thenReturn(reimbursed);

        ResponseEntity<Expense> response = controller.markAsReimbursed(10L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reimbursed, response.getBody());
    }

    @Test
    void testExportExpensesToCsv() {
        when(authentication.getName()).thenReturn("testuser");

        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(1L);
        when(expense.getDescription()).thenReturn("Hotel \"Stay\"");
        when(expense.getAmount()).thenReturn(new BigDecimal("120.50"));
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2025, 3, 1));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(expense.getHasReceipt()).thenReturn(true);
        when(expense.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 3, 1, 10, 0));

        when(expenseService.getExpensesForUserWithDateRange("testuser", "2025-01-01", "2025-03-31"))
                .thenReturn(List.of(expense));

        ResponseEntity<Resource> response = controller.exportExpensesToCsv("2025-01-01", "2025-03-31", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}