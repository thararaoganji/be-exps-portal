package portal.expenses.service;

import portal.expenses.dto.ExpenseFilterRequest;
import portal.expenses.dto.PageResponse;
import portal.expenses.entity.AppUser;
import portal.expenses.entity.ApprovalStatus;
import portal.expenses.entity.AuditEntry;
import portal.expenses.entity.Expense;
import portal.expenses.entity.ExpenseCategory;
import portal.expenses.repository.AuditEntryRepository;
import portal.expenses.repository.ExpenseRepository;
import portal.expenses.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService Unit Tests")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private LocalStorageService localStorageService;

    @Mock
    private PolicyEngineService policyEngineService;

    @Mock
    private AuditEntryRepository auditEntryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private AppUser testUser;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setUsername("test.user");
        testUser.setEmail("test@example.com");

        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setDescription("Test Expense");
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setUser(testUser);
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setExpenseDate(LocalDate.now());
        testExpense.setApprovalStatus(ApprovalStatus.PENDING);
    }

    @Test
    @DisplayName("Test 1: Create expense successfully without receipt")
    void testCreateExpense_WithoutReceipt_Success() throws IOException {
        // Arrange
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));

        // Mock save to properly update the expense with hasReceipt = false
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            if (expense.getId() == null) {
                expense.setId(1L);
            }
            return expense;
        });

        PolicyEngineService.ApprovalDecision decision = new PolicyEngineService.ApprovalDecision();
        decision.setFinalStatus(ApprovalStatus.PENDING);
        decision.setReason("Standard approval required");
        when(policyEngineService.evaluateExpense(any(Expense.class))).thenReturn(decision);

        // Act
        Expense result = expenseService.createExpense(
            "Test Expense",
            new BigDecimal("100.00"),
            null,
            "test.user",
            ExpenseCategory.MEALS,
            LocalDate.now()
        );

        // Assert
        assertNotNull(result);
        assertEquals("Test Expense", result.getDescription());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(Boolean.FALSE, result.getHasReceipt());
        verify(expenseRepository, times(2)).save(any(Expense.class));
        verify(auditEntryRepository, times(2)).save(any(AuditEntry.class));
    }

    @Test
    @DisplayName("Test 2: Create expense with receipt - local storage")
    void testCreateExpense_WithReceipt_LocalStorage() throws IOException {
        // Arrange
        MockMultipartFile receipt = new MockMultipartFile(
            "receipt", "test.pdf", "application/pdf", "test content".getBytes()
        );

        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));

        // Mock save to properly update the expense
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            if (expense.getId() == null) {
                expense.setId(1L);
            }
            return expense;
        });

        PolicyEngineService.ApprovalDecision decision = new PolicyEngineService.ApprovalDecision();
        decision.setFinalStatus(ApprovalStatus.PENDING);
        decision.setReason("Standard approval required");
        when(policyEngineService.evaluateExpense(any(Expense.class))).thenReturn(decision);

        // Act
        Expense result = expenseService.createExpense(
            "Expense with receipt",
            new BigDecimal("200.00"),
            receipt,
            "test.user",
            ExpenseCategory.TRAVEL,
            LocalDate.now()
        );

        // Assert
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result.getHasReceipt());
        verify(localStorageService).uploadFile(anyString(), any(byte[].class));
        verify(s3Service, never()).uploadFile(anyString(), any(byte[].class));
    }

    @Test
    @DisplayName("Test 3: Create expense throws exception when user not found")
    void testCreateExpense_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("unknown.user")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            expenseService.createExpense(
                "Test",
                new BigDecimal("100.00"),
                null,
                "unknown.user",
                ExpenseCategory.OTHER,
                LocalDate.now()
            );
        });

        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 4: Submit expense successfully")
    void testSubmitExpense_Success() {
        // Arrange
        testExpense.setApprovalStatus(ApprovalStatus.PENDING);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        PolicyEngineService.ApprovalDecision decision = new PolicyEngineService.ApprovalDecision();
        decision.setFinalStatus(ApprovalStatus.MANAGER_REVIEW);
        decision.setReason("Requires manager approval");
        when(policyEngineService.evaluateExpense(any(Expense.class))).thenReturn(decision);

        // Act
        Expense result = expenseService.submitExpense(1L, "test.user");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getSubmittedAt());
        verify(expenseRepository).save(any(Expense.class));
        verify(auditEntryRepository).save(any(AuditEntry.class));
    }

    @Test
    @DisplayName("Test 5: Submit expense throws exception when not owner")
    void testSubmitExpense_NotOwner_ThrowsException() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.submitExpense(1L, "different.user");
        });

        assertTrue(exception.getMessage().contains("not authorized"));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 6: Get expenses for user")
    void testGetExpensesForCurrentUser_Success() {
        // Arrange
        List<Expense> expectedExpenses = Arrays.asList(testExpense);
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByUserId(1L)).thenReturn(expectedExpenses);

        // Act
        List<Expense> result = expenseService.getExpensesForCurrentUser("test.user");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExpense, result.get(0));
        verify(expenseRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Test 7: Get expense by ID successfully")
    void testGetExpenseById_Success() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        // Act
        Expense result = expenseService.getExpenseById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Expense", result.getDescription());
    }

    @Test
    @DisplayName("Test 8: Get expense by ID throws exception when not found")
    void testGetExpenseById_NotFound_ThrowsException() {
        // Arrange
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.getExpenseById(999L);
        });
    }

    @Test
    @DisplayName("Test 9: Mark expense as reimbursed successfully")
    void testMarkAsReimbursed_Success() {
        // Arrange
        testExpense.setApprovalStatus(ApprovalStatus.APPROVED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        // Act
        Expense result = expenseService.markAsReimbursed(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ApprovalStatus.REIMBURSED, result.getApprovalStatus());
        assertNotNull(result.getReimbursedAt());
        verify(expenseRepository).save(any(Expense.class));
        verify(auditEntryRepository).save(any(AuditEntry.class));
    }

    @Test
    @DisplayName("Test 10: Mark as reimbursed throws exception when not approved")
    void testMarkAsReimbursed_NotApproved_ThrowsException() {
        // Arrange
        testExpense.setApprovalStatus(ApprovalStatus.PENDING);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            expenseService.markAsReimbursed(1L);
        });

        assertTrue(exception.getMessage().contains("Only approved expenses"));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 11: Get expenses by status")
    void testGetExpensesByStatus_Success() {
        // Arrange
        List<Expense> expectedExpenses = Arrays.asList(testExpense);
        when(expenseRepository.findByApprovalStatus(ApprovalStatus.PENDING))
            .thenReturn(expectedExpenses);

        // Act
        List<Expense> result = expenseService.getExpensesByStatus(ApprovalStatus.PENDING);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ApprovalStatus.PENDING, result.get(0).getApprovalStatus());
    }

    @Test
    @DisplayName("Test 12: Get audit log for expense")
    void testGetAuditLog_Success() {
        // Arrange
        AuditEntry entry1 = new AuditEntry();
        entry1.setId(1L);
        entry1.setAction("CREATED");

        AuditEntry entry2 = new AuditEntry();
        entry2.setId(2L);
        entry2.setAction("SUBMITTED");

        List<AuditEntry> expectedAudit = Arrays.asList(entry1, entry2);
        when(auditEntryRepository.findByExpenseIdOrderByCreatedAtAsc(1L))
            .thenReturn(expectedAudit);

        // Act
        List<AuditEntry> result = expenseService.getAuditLog(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CREATED", result.get(0).getAction());
        assertEquals("SUBMITTED", result.get(1).getAction());
    }

    @Test
    @DisplayName("Test 13: Create expense with S3 storage mode and draft = true")
    void testCreateExpense_Draft_S3Storage() throws IOException {
        ReflectionTestUtils.setField(expenseService, "storageMode", "s3");
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));
        MockMultipartFile receipt = new MockMultipartFile("receipt", "rec.jpg", "image/jpeg", "image bytes".getBytes());

        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        Expense result = expenseService.createExpense(
            null, null, receipt, "test.user", null, null, true
        );

        assertNotNull(result);
        assertEquals(ApprovalStatus.DRAFT, result.getApprovalStatus());
        assertEquals(ExpenseCategory.OTHER, result.getCategory());
        assertNotNull(result.getExpenseDate());
        verify(s3Service).uploadFile(anyString(), any(byte[].class));
        verify(auditEntryRepository, times(1)).save(any(AuditEntry.class));
        verify(policyEngineService, never()).evaluateExpense(any());
    }

    @Test
    @DisplayName("Test 14: Submit expense throws exception when status is not DRAFT or PENDING")
    void testSubmitExpense_InvalidStatus_ThrowsException() {
        testExpense.setApprovalStatus(ApprovalStatus.APPROVED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            expenseService.submitExpense(1L, "test.user")
        );
        assertTrue(ex.getMessage().contains("Only DRAFT or PENDING expenses"));
    }

    @Test
    @DisplayName("Test 15: Submit expense user not found for audit throws exception")
    void testSubmitExpense_AuditUserNotFound_ThrowsException() {
        testExpense.setApprovalStatus(ApprovalStatus.DRAFT);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        PolicyEngineService.ApprovalDecision decision = new PolicyEngineService.ApprovalDecision();
        decision.setFinalStatus(ApprovalStatus.APPROVED);
        decision.setReason("Auto-approved");
        when(policyEngineService.evaluateExpense(any(Expense.class))).thenReturn(decision);
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            expenseService.submitExpense(1L, "test.user")
        );
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    @DisplayName("Test 16: Get expenses for user with ID")
    void testGetExpensesForUser() {
        when(expenseRepository.findByUserId(1L)).thenReturn(List.of(testExpense));
        List<Expense> expenses = expenseService.getExpensesForUser(1L);
        assertEquals(1, expenses.size());
    }

    @Test
    @DisplayName("Test 17: Get expenses for current user throws if user not found")
    void testGetExpensesForCurrentUser_UserNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> expenseService.getExpensesForCurrentUser("missing"));
    }

    @Test
    @DisplayName("Test 18: Update draft expense successfully with S3 and local storage")
    void testUpdateExpense_Success_S3AndLocalStorage() throws IOException {
        ReflectionTestUtils.setField(expenseService, "storageMode", "s3");
        testExpense.setApprovalStatus(ApprovalStatus.DRAFT);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "png data".getBytes());
        LocalDate newDate = LocalDate.now().minusDays(1);

        Expense updated = expenseService.updateExpense(
            1L, "Updated desc", new BigDecimal("50.00"), file, "test.user", ExpenseCategory.EQUIPMENT, newDate
        );

        assertEquals("Updated desc", updated.getDescription());
        assertEquals(new BigDecimal("50.00"), updated.getAmount());
        assertEquals(ExpenseCategory.EQUIPMENT, updated.getCategory());
        assertEquals(newDate, updated.getExpenseDate());
        verify(s3Service).uploadFile(anyString(), any(byte[].class));
        verify(auditEntryRepository).save(any(AuditEntry.class));

        // Test with local storage
        ReflectionTestUtils.setField(expenseService, "storageMode", "local");
        expenseService.updateExpense(
            1L, null, null, file, "test.user", null, null
        );
        verify(localStorageService).uploadFile(anyString(), any(byte[].class));
    }

    @Test
    @DisplayName("Test 19: Update expense failure branches")
    void testUpdateExpense_FailureBranches() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            expenseService.updateExpense(99L, "desc", BigDecimal.ONE, null, "test.user", null, null)
        );

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        assertThrows(RuntimeException.class, () ->
            expenseService.updateExpense(1L, "desc", BigDecimal.ONE, null, "other.user", null, null)
        );

        testExpense.setApprovalStatus(ApprovalStatus.APPROVED);
        assertThrows(IllegalStateException.class, () ->
            expenseService.updateExpense(1L, "desc", BigDecimal.ONE, null, "test.user", null, null)
        );

        testExpense.setApprovalStatus(ApprovalStatus.DRAFT);
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            expenseService.updateExpense(1L, "desc", BigDecimal.ONE, null, "test.user", null, null)
        );
    }

    @Test
    @DisplayName("Test 20: Get expenses for user with date range")
    void testGetExpensesForUserWithDateRange() {
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(testExpense));
        when(expenseRepository.findByUser(testUser)).thenReturn(List.of(testExpense));

        List<Expense> withDates = expenseService.getExpensesForUserWithDateRange("test.user", "2023-01-01", "2023-01-31");
        assertEquals(1, withDates.size());

        List<Expense> withoutDates = expenseService.getExpensesForUserWithDateRange("test.user", null, null);
        assertEquals(1, withoutDates.size());

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () ->
            expenseService.getExpensesForUserWithDateRange("unknown", null, null)
        );
    }

    @Test
    @DisplayName("Test 21: Get expenses with filters")
    void testGetExpensesWithFilters() {
        ExpenseFilterRequest request = new ExpenseFilterRequest();
        request.setSortDirection("DESC");
        request.setSortBy("expenseDate");
        request.setPage(0);
        request.setSize(10);
        request.setStatus(ApprovalStatus.PENDING);
        request.setCategory(ExpenseCategory.MEALS);
        request.setMinAmount(BigDecimal.ZERO);
        request.setMaxAmount(BigDecimal.TEN);
        request.setStartDate(LocalDate.now().minusDays(5));
        request.setEndDate(LocalDate.now());
        request.setHasReceipt(true);

        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));
        Page<Expense> page = new PageImpl<>(List.of(testExpense));
        when(expenseRepository.findByFilters(
            eq(testUser.getId()), eq(ApprovalStatus.PENDING), eq(ExpenseCategory.MEALS),
            eq(BigDecimal.ZERO), eq(BigDecimal.TEN), any(), any(), eq(true), any(Pageable.class)
        )).thenReturn(page);

        PageResponse<Expense> response = expenseService.getExpensesWithFilters("test.user", request);
        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () ->
            expenseService.getExpensesWithFilters("unknown", request)
        );
    }

    @Test
    @DisplayName("Test 22: Get all expenses with filters (ASC direction)")
    void testGetAllExpensesWithFilters() {
        ExpenseFilterRequest request = new ExpenseFilterRequest();
        request.setSortDirection("ASC");
        request.setSortBy("amount");
        request.setPage(0);
        request.setSize(5);

        Page<Expense> page = new PageImpl<>(Collections.emptyList());
        when(expenseRepository.findByFilters(
            isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
        )).thenReturn(page);

        PageResponse<Expense> response = expenseService.getAllExpensesWithFilters(request);
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
    }

    @Test
    @DisplayName("Test 23: Get expenses by status with pagination")
    void testGetExpensesByStatusWithPagination() {
        Page<Expense> page = new PageImpl<>(List.of(testExpense));
        when(expenseRepository.findByApprovalStatus(eq(ApprovalStatus.PENDING), any(Pageable.class)))
            .thenReturn(page);

        PageResponse<Expense> response = expenseService.getExpensesByStatusWithPagination(
            ApprovalStatus.PENDING, 0, 10, "id", "DESC"
        );
        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        PageResponse<Expense> responseAsc = expenseService.getExpensesByStatusWithPagination(
            ApprovalStatus.PENDING, 0, 10, "id", "ASC"
        );
        assertNotNull(responseAsc);
    }
}