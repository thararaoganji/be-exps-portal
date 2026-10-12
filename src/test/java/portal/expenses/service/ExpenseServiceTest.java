package portal.expenses.service;

import portal.expenses.dto.ExpenseFilterRequest;
import portal.expenses.dto.PageResponse;
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
import portal.expenses.entity.*;

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
    @DisplayName("Test 13: Create expense as draft with null category and null expenseDate")
    void testCreateExpense_AsDraft_NullCategoryAndDate() throws IOException {
        ReflectionTestUtils.setField(expenseService, "storageMode", "local");
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense e = invocation.getArgument(0);
            e.setId(5L);
            return e;
        });

        Expense result = expenseService.createExpense(
                "Draft expense",
                new BigDecimal("50.00"),
                null,
                "test.user",
                null,
                null,
                true
        );

        assertNotNull(result);
        assertEquals(ApprovalStatus.DRAFT, result.getApprovalStatus());
        assertEquals(ExpenseCategory.OTHER, result.getCategory());
        assertNotNull(result.getExpenseDate());
        verify(policyEngineService, never()).evaluateExpense(any());
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(auditEntryRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    @DisplayName("Test 14: Create expense with receipt - S3 storage mode")
    void testCreateExpense_WithReceipt_S3Storage() throws IOException {
        ReflectionTestUtils.setField(expenseService, "storageMode", "s3");
        MockMultipartFile receipt = new MockMultipartFile(
                "receipt", "receipt.png", "image/png", "sample bytes".getBytes()
        );

        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyEngineService.ApprovalDecision decision = new PolicyEngineService.ApprovalDecision();
        decision.setFinalStatus(ApprovalStatus.APPROVED);
        decision.setReason("Auto approved");
        when(policyEngineService.evaluateExpense(any(Expense.class))).thenReturn(decision);

        Expense result = expenseService.createExpense(
                "S3 Expense",
                new BigDecimal("75.00"),
                receipt,
                "test.user",
                ExpenseCategory.OFFICE_SUPPLIES,
                LocalDate.now(),
                false
        );

        assertNotNull(result);
        assertTrue(result.getHasReceipt());
        verify(s3Service).uploadFile(anyString(), any(byte[].class));
        verify(localStorageService, never()).uploadFile(anyString(), any(byte[].class));
    }

    @Test
    @DisplayName("Test 15: Get expenses for user ID")
    void testGetExpensesForUser() {
        when(expenseRepository.findByUserId(1L)).thenReturn(Collections.singletonList(testExpense));

        List<Expense> result = expenseService.getExpensesForUser(1L);

        assertEquals(1, result.size());
        assertEquals(testExpense, result.get(0));
        verify(expenseRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Test 16: Get expenses for current user throws exception when user not found")
    void testGetExpensesForCurrentUser_UserNotFound() {
        when(userRepository.findByUsername("missing.user")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                expenseService.getExpensesForCurrentUser("missing.user")
        );
    }

    @Test
    @DisplayName("Test 17: Submit expense throws when expense not found")
    void testSubmitExpense_ExpenseNotFound() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                expenseService.submitExpense(99L, "test.user")
        );
    }

    @Test
    @DisplayName("Test 18: Submit expense throws when status not DRAFT or PENDING")
    void testSubmitExpense_InvalidStatus() {
        testExpense.setApprovalStatus(ApprovalStatus.APPROVED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        assertThrows(IllegalStateException.class, () ->
                expenseService.submitExpense(1L, "test.user")
        );
    }

    @Test
    @DisplayName("Test 19: Submit expense throws when user not found after evaluation")
    void testSubmitExpense_UserNotFoundAfterEval() {
        testExpense.setApprovalStatus(ApprovalStatus.DRAFT);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        PolicyEngineService.ApprovalDecision decision = new PolicyEngineService.ApprovalDecision();
        decision.setFinalStatus(ApprovalStatus.PENDING);
        decision.setReason("Evaluating");
        when(policyEngineService.evaluateExpense(any(Expense.class))).thenReturn(decision);
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                expenseService.submitExpense(1L, "test.user")
        );
    }

    @Test
    @DisplayName("Test 20: Update expense successfully with S3 upload and all fields")
    void testUpdateExpense_Success_S3Upload() throws IOException {
        ReflectionTestUtils.setField(expenseService, "storageMode", "s3");
        testExpense.setApprovalStatus(ApprovalStatus.DRAFT);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));

        MockMultipartFile receipt = new MockMultipartFile(
                "receipt", "new_receipt.pdf", "application/pdf", "data".getBytes()
        );

        Expense updated = expenseService.updateExpense(
                1L,
                "Updated description",
                new BigDecimal("150.00"),
                receipt,
                "test.user",
                ExpenseCategory.TRAVEL,
                LocalDate.of(2025, 1, 15)
        );

        assertNotNull(updated);
        assertEquals("Updated description", updated.getDescription());
        assertEquals(new BigDecimal("150.00"), updated.getAmount());
        assertEquals(ExpenseCategory.TRAVEL, updated.getCategory());
        assertEquals(LocalDate.of(2025, 1, 15), updated.getExpenseDate());
        assertTrue(updated.getHasReceipt());
        verify(s3Service).uploadFile(anyString(), any(byte[].class));
        verify(auditEntryRepository).save(any(AuditEntry.class));
    }

    @Test
    @DisplayName("Test 21: Update expense successfully with local storage and null fields")
    void testUpdateExpense_Success_LocalStorage_NullFields() throws IOException {
        ReflectionTestUtils.setField(expenseService, "storageMode", "local");
        testExpense.setApprovalStatus(ApprovalStatus.DRAFT);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));

        MockMultipartFile receipt = new MockMultipartFile(
                "receipt", "receipt.pdf", "application/pdf", "data".getBytes()
        );

        Expense updated = expenseService.updateExpense(
                1L,
                null,
                null,
                receipt,
                "test.user",
                null,
                null
        );

        assertNotNull(updated);
        verify(localStorageService).uploadFile(anyString(), any(byte[].class));
    }

    @Test
    @DisplayName("Test 22: Update expense throws when expense not found")
    void testUpdateExpense_NotFound() {
        when(expenseRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                expenseService.updateExpense(10L, "desc", null, null, "test.user", null, null)
        );
    }

    @Test
    @DisplayName("Test 23: Update expense throws when not owner")
    void testUpdateExpense_NotOwner() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        assertThrows(RuntimeException.class, () ->
                expenseService.updateExpense(1L, "desc", null, null, "other.user", null, null)
        );
    }

    @Test
    @DisplayName("Test 24: Update expense throws when status not DRAFT")
    void testUpdateExpense_NotDraft() {
        testExpense.setApprovalStatus(ApprovalStatus.PENDING);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        assertThrows(IllegalStateException.class, () ->
                expenseService.updateExpense(1L, "desc", null, null, "test.user", null, null)
        );
    }

    @Test
    @DisplayName("Test 25: Update expense throws when user not found after save")
    void testUpdateExpense_UserNotFoundAfterSave() {
        testExpense.setApprovalStatus(ApprovalStatus.DRAFT);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                expenseService.updateExpense(1L, "desc", null, null, "test.user", null, null)
        );
    }

    @Test
    @DisplayName("Test 26: Mark as reimbursed throws when expense not found")
    void testMarkAsReimbursed_NotFound() {
        when(expenseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                expenseService.markAsReimbursed(404L)
        );
    }

    @Test
    @DisplayName("Test 27: Get expenses for user with date range")
    void testGetExpensesForUserWithDateRange() {
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(testExpense));

        List<Expense> result = expenseService.getExpensesForUserWithDateRange("test.user", "2025-01-01", "2025-01-31");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(expenseRepository).findByUserAndExpenseDateBetween(eq(testUser), eq(LocalDate.parse("2025-01-01")), eq(LocalDate.parse("2025-01-31")));
    }

    @Test
    @DisplayName("Test 28: Get expenses for user without date range")
    void testGetExpensesForUserWithDateRange_NullDates() {
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByUser(testUser)).thenReturn(Collections.singletonList(testExpense));

        List<Expense> result = expenseService.getExpensesForUserWithDateRange("test.user", null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(expenseRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("Test 29: Get expenses for user with date range throws when user not found")
    void testGetExpensesForUserWithDateRange_UserNotFound() {
        when(userRepository.findByUsername("missing.user")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                expenseService.getExpensesForUserWithDateRange("missing.user", "2025-01-01", "2025-01-31")
        );
    }

    @Test
    @DisplayName("Test 30: Get expenses with filters for user")
    void testGetExpensesWithFilters() {
        when(userRepository.findByUsername("test.user")).thenReturn(Optional.of(testUser));

        ExpenseFilterRequest filter = new ExpenseFilterRequest();
        filter.setPage(0);
        filter.setSize(10);
        filter.setSortBy("createdAt");
        filter.setSortDirection("DESC");
        filter.setStatus(ApprovalStatus.PENDING);
        filter.setCategory(ExpenseCategory.MEALS);
        filter.setMinAmount(new BigDecimal("10"));
        filter.setMaxAmount(new BigDecimal("500"));
        filter.setStartDate(LocalDate.of(2025, 1, 1));
        filter.setEndDate(LocalDate.of(2025, 1, 31));
        filter.setHasReceipt(true);

        Page<Expense> page = new PageImpl<>(Collections.singletonList(testExpense));
        when(expenseRepository.findByFilters(
                eq(1L),
                eq(ApprovalStatus.PENDING),
                eq(ExpenseCategory.MEALS),
                eq(new BigDecimal("10")),
                eq(new BigDecimal("500")),
                eq(LocalDate.of(2025, 1, 1)),
                eq(LocalDate.of(2025, 1, 31)),
                eq(true),
                any(Pageable.class)
        )).thenReturn(page);

        PageResponse<Expense> response = expenseService.getExpensesWithFilters("test.user", filter);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertTrue(response.isFirst());
    }

    @Test
    @DisplayName("Test 31: Get expenses with filters user not found")
    void testGetExpensesWithFilters_UserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        ExpenseFilterRequest filter = new ExpenseFilterRequest();
        filter.setSortBy("id");
        filter.setSortDirection("ASC");

        assertThrows(UsernameNotFoundException.class, () ->
                expenseService.getExpensesWithFilters("unknown", filter)
        );
    }

    @Test
    @DisplayName("Test 32: Get all expenses with filters (manager/finance)")
    void testGetAllExpensesWithFilters() {
        ExpenseFilterRequest filter = new ExpenseFilterRequest();
        filter.setPage(1);
        filter.setSize(5);
        filter.setSortBy("amount");
        filter.setSortDirection("ASC");

        Page<Expense> page = new PageImpl<>(Collections.singletonList(testExpense));
        when(expenseRepository.findByFilters(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        PageResponse<Expense> response = expenseService.getAllExpensesWithFilters(filter);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    @DisplayName("Test 33: Get expenses by status with pagination")
    void testGetExpensesByStatusWithPagination() {
        Page<Expense> page = new PageImpl<>(Collections.singletonList(testExpense));
        when(expenseRepository.findByApprovalStatus(eq(ApprovalStatus.PENDING), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<Expense> response = expenseService.getExpensesByStatusWithPagination(
                ApprovalStatus.PENDING, 0, 10, "expenseDate", "DESC"
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(expenseRepository).findByApprovalStatus(eq(ApprovalStatus.PENDING), any(Pageable.class));
    }
}