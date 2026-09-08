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
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import portal.expenses.dto.*;
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
import java.util.Collections;
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

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ExpenseController expenseController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(expenseController, "storageMode", "local");
    }

    @Test
    void testCreateExpense() throws IOException {
        when(authentication.getName()).thenReturn("testuser");
        ExpenseRequest request = mock(ExpenseRequest.class);
        when(request.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(request.getDescription()).thenReturn("Flight");
        when(request.getExpenseDate()).thenReturn(LocalDate.now());

        Expense createdExpense = mock(Expense.class);
        when(createdExpense.getId()).thenReturn(1L);
        when(createdExpense.getApprovalStatus()).thenReturn(ApprovalStatus.SUBMITTED);

        when(expenseService.createExpense(anyString(), any(), any(), anyString(), any(), any(), eq(false)))
                .thenReturn(createdExpense);

        ResponseEntity<Expense> response = expenseController.createExpense(request, multipartFile, false, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdExpense, response.getBody());
    }

    @Test
    void testGetMyExpenses() {
        when(authentication.getName()).thenReturn("testuser");
        List<Expense> expenses = Collections.singletonList(mock(Expense.class));
        when(expenseService.getExpensesForCurrentUser("testuser")).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = expenseController.getMyExpenses(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
    }

    @Test
    void testGetMyExpensesWithFilters() {
        when(authentication.getName()).thenReturn("testuser");
        ExpenseFilterRequest filter = mock(ExpenseFilterRequest.class);
        when(filter.getPage()).thenReturn(0);
        when(filter.getSize()).thenReturn(10);
        when(filter.getSortBy()).thenReturn("createdAt");
        when(filter.getSortDirection()).thenReturn("DESC");

        @SuppressWarnings("unchecked")
        PageResponse<Expense> pageResponse = mock(PageResponse.class);
        when(pageResponse.getContent()).thenReturn(Collections.emptyList());
        when(pageResponse.getPageNumber()).thenReturn(0);
        when(pageResponse.getTotalPages()).thenReturn(1);

        when(expenseService.getExpensesWithFilters("testuser", filter)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<Expense>> response = expenseController.getMyExpensesWithFilters(filter, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
    }

    @Test
    void testGetPendingApprovals() {
        when(authentication.getName()).thenReturn("manager");
        List<Expense> expenses = Collections.singletonList(mock(Expense.class));
        when(expenseService.getExpensesByStatus(ApprovalStatus.MANAGER_REVIEW)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = expenseController.getPendingApprovals(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
    }

    @Test
    void testGetApprovedExpenses() {
        List<Expense> expenses = Collections.singletonList(mock(Expense.class));
        when(expenseService.getExpensesByStatus(ApprovalStatus.APPROVED)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = expenseController.getApprovedExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
    }

    @Test
    void testGetManagerQueue() {
        List<Expense> expenses = Collections.singletonList(mock(Expense.class));
        when(expenseService.getExpensesByStatus(ApprovalStatus.MANAGER_REVIEW)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = expenseController.getManagerQueue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
    }

    @Test
    void testGetFinanceQueue() {
        List<Expense> expenses = Collections.singletonList(mock(Expense.class));
        when(expenseService.getExpensesByStatus(ApprovalStatus.FINANCE_REVIEW)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = expenseController.getFinanceQueue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
    }

    @Test
    void testGetReimbursedExpenses() {
        List<Expense> expenses = Collections.singletonList(mock(Expense.class));
        when(expenseService.getExpensesByStatus(ApprovalStatus.REIMBURSED)).thenReturn(expenses);

        ResponseEntity<List<Expense>> response = expenseController.getReimbursedExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expenses, response.getBody());
    }

    @Test
    void testGetExpense() {
        Expense expense = mock(Expense.class);
        when(expenseService.getExpenseById(1L)).thenReturn(expense);

        ResponseEntity<Expense> response = expenseController.getExpense(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expense, response.getBody());
    }

    @Test
    void testGetExpenseDetails() {
        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(1L);
        when(expenseService.getExpenseById(1L)).thenReturn(expense);

        ExpenseApproval approval = mock(ExpenseApproval.class);
        when(approval.getId()).thenReturn(10L);
        when(approval.getExpense()).thenReturn(expense);
        when(approval.getApprovalLevel()).thenReturn("MANAGER");
        when(approval.getStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(approval.getComments()).thenReturn("Approved comment");
        when(approval.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(approval.getApprover()).thenReturn(null);
        when(approval.getAppliedRule()).thenReturn(null);

        when(approvalService.getApprovalHistory(1L)).thenReturn(Collections.singletonList(approval));

        ResponseEntity<ExpenseDetailResponse> response = expenseController.getExpenseDetails(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expense, response.getBody().getExpense());
        assertEquals(1, response.getBody().getApprovals().size());
    }

    @Test
    void testSubmitExpense() {
        when(authentication.getName()).thenReturn("testuser");
        Expense submittedExpense = mock(Expense.class);
        when(submittedExpense.getApprovalStatus()).thenReturn(ApprovalStatus.SUBMITTED);
        when(expenseService.submitExpense(1L, "testuser")).thenReturn(submittedExpense);

        ResponseEntity<Expense> response = expenseController.submitExpense(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(submittedExpense, response.getBody());
    }

    @Test
    void testUpdateExpense() throws IOException {
        when(authentication.getName()).thenReturn("testuser");
        ExpenseRequest request = mock(ExpenseRequest.class);
        when(request.getDescription()).thenReturn("Updated desc");
        when(request.getAmount()).thenReturn(new BigDecimal("200.00"));
        when(request.getExpenseDate()).thenReturn(LocalDate.now());

        Expense updatedExpense = mock(Expense.class);
        when(updatedExpense.getApprovalStatus()).thenReturn(ApprovalStatus.DRAFT);

        when(expenseService.updateExpense(eq(1L), anyString(), any(), any(), eq("testuser"), any(), any()))
                .thenReturn(updatedExpense);

        ResponseEntity<Expense> response = expenseController.updateExpense(1L, request, multipartFile, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedExpense, response.getBody());
    }

    @Test
    void testGetReceipt_NullKey() {
        Expense expense = mock(Expense.class);
        when(expense.getS3ObjectKey()).thenReturn(null);
        when(expenseService.getExpenseById(1L)).thenReturn(expense);

        ResponseEntity<Resource> response = expenseController.getReceipt(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetReceipt_LocalStorage() {
        Expense expense = mock(Expense.class);
        when(expense.getS3ObjectKey()).thenReturn("receipt123.png");
        when(expenseService.getExpenseById(1L)).thenReturn(expense);
        when(localStorageService.downloadFile("receipt123.png")).thenReturn("fileContent".getBytes());

        ReflectionTestUtils.setField(expenseController, "storageMode", "local");

        ResponseEntity<Resource> response = expenseController.getReceipt(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("receipt123.png"));
    }

    @Test
    void testGetReceipt_S3Storage() {
        Expense expense = mock(Expense.class);
        when(expense.getS3ObjectKey()).thenReturn("receipt123.png");
        when(expenseService.getExpenseById(1L)).thenReturn(expense);
        when(s3Service.downloadFile("receipt123.png")).thenReturn("fileContent".getBytes());

        ReflectionTestUtils.setField(expenseController, "storageMode", "s3");

        ResponseEntity<Resource> response = expenseController.getReceipt(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testApproveExpense_Success() {
        when(authentication.getName()).thenReturn("approverUser");
        ApprovalRequest request = mock(ApprovalRequest.class);
        when(request.getStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(request.getComments()).thenReturn("Looks good");

        Expense approvedExpense = mock(Expense.class);
        when(approvedExpense.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);

        when(approvalService.processApproval(1L, "approverUser", ApprovalStatus.APPROVED, "Looks good"))
                .thenReturn(approvedExpense);

        ResponseEntity<Expense> response = expenseController.approveExpense(1L, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(approvedExpense, response.getBody());
    }

    @Test
    void testApproveExpense_NullStatusThrowsException() {
        when(authentication.getName()).thenReturn("approverUser");
        ApprovalRequest request = mock(ApprovalRequest.class);
        when(request.getStatus()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                expenseController.approveExpense(1L, request, authentication));
    }

    @Test
    void testGetApprovalHistory() {
        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(1L);

        ExpenseApproval approval = mock(ExpenseApproval.class);
        when(approval.getId()).thenReturn(5L);
        when(approval.getExpense()).thenReturn(expense);
        when(approval.getApprovalLevel()).thenReturn("FINANCE");
        when(approval.getStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(approval.getComments()).thenReturn("Paid");
        when(approval.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(approval.getApprover()).thenReturn(null);
        when(approval.getAppliedRule()).thenReturn(null);

        when(approvalService.getApprovalHistory(1L)).thenReturn(Collections.singletonList(approval));

        ResponseEntity<List<ExpenseApprovalResponse>> response = expenseController.getApprovalHistory(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(5L, response.getBody().get(0).getId());
    }

    @Test
    void testGetAuditLog() {
        AuditEntry auditEntry = mock(AuditEntry.class);
        when(auditEntry.getId()).thenReturn(100L);
        when(auditEntry.getAction()).thenReturn("UPDATE");
        when(auditEntry.getOldStatus()).thenReturn("DRAFT");
        when(auditEntry.getNewStatus()).thenReturn("SUBMITTED");
        when(auditEntry.getComment()).thenReturn("Audit comment");
        when(auditEntry.getPerformedBy()).thenReturn("testuser");
        when(auditEntry.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(expenseService.getAuditLog(1L)).thenReturn(Collections.singletonList(auditEntry));

        ResponseEntity<List<AuditLogResponse>> response = expenseController.getAuditLog(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(100L, response.getBody().get(0).getId());
        assertEquals("UPDATE", response.getBody().get(0).getAction());
    }

    @Test
    void testMarkAsReimbursed() {
        when(authentication.getName()).thenReturn("financeUser");
        Expense expense = mock(Expense.class);
        when(expenseService.markAsReimbursed(1L)).thenReturn(expense);

        ResponseEntity<Expense> response = expenseController.markAsReimbursed(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expense, response.getBody());
    }

    @Test
    void testExportExpensesToCsv() {
        when(authentication.getName()).thenReturn("testuser");
        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(1L);
        when(expense.getDescription()).thenReturn("Lunch \"with\" client");
        when(expense.getAmount()).thenReturn(new BigDecimal("50.00"));
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 1, 15));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(expense.getHasReceipt()).thenReturn(true);
        when(expense.getCreatedAt()).thenReturn(LocalDateTime.of(2023, 1, 15, 10, 0));

        when(expenseService.getExpensesForUserWithDateRange("testuser", "2023-01-01", "2023-01-31"))
                .thenReturn(Collections.singletonList(expense));

        ResponseEntity<Resource> response = expenseController.exportExpensesToCsv("2023-01-01", "2023-01-31", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.parseMediaType("text/csv"), response.getHeaders().getContentType());
        assertNotNull(response.getBody());
    }
}