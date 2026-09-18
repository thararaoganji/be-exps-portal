package portal.expenses.service;

import portal.expenses.repository.AuditEntryRepository;
import portal.expenses.repository.ExpenseApprovalRepository;
import portal.expenses.repository.ExpenseRepository;
import portal.expenses.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portal.expenses.entity.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalService Unit Tests")
class ApprovalServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseApprovalRepository expenseApprovalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditEntryRepository auditEntryRepository;

    @InjectMocks
    private ApprovalService approvalService;

    private Expense testExpense;
    private AppUser manager;
    private AppUser finance;

    @BeforeEach
    void setUp() {
        Role managerRole = new Role();
        managerRole.setId(2L);
        managerRole.setName("ROLE_MANAGER");

        Role financeRole = new Role();
        financeRole.setId(3L);
        financeRole.setName("ROLE_FINANCE");

        manager = new AppUser();
        manager.setId(2L);
        manager.setUsername("manager.john");
        manager.setRoles(java.util.Collections.singleton(managerRole));

        finance = new AppUser();
        finance.setId(3L);
        finance.setUsername("finance.jane");
        finance.setRoles(java.util.Collections.singleton(financeRole));

        AppUser employee = new AppUser();
        employee.setId(1L);
        employee.setUsername("employee.user");

        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setDescription("Test Expense");
        testExpense.setAmount(new BigDecimal("500.00"));
        testExpense.setUser(employee);
        testExpense.setApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
    }

    @Test
    @DisplayName("Test 1: Manager approves expense successfully")
    void testProcessApproval_ManagerApproves_Success() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("manager.john")).thenReturn(Optional.of(manager));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseApprovalRepository.findByExpenseIdOrderByCreatedAtAsc(1L))
            .thenReturn(Arrays.asList());

        // Act
        Expense result = approvalService.processApproval(
            1L, "manager.john", ApprovalStatus.APPROVED, "Looks good"
        );

        // Assert
        assertNotNull(result);
        assertEquals(ApprovalStatus.APPROVED, result.getApprovalStatus());
        assertNotNull(result.getApprovedAt());
        verify(expenseRepository).save(any(Expense.class));
        verify(expenseApprovalRepository).save(any(ExpenseApproval.class));
        verify(auditEntryRepository).save(any(AuditEntry.class));
    }

    @Test
    @DisplayName("Test 2: Manager rejects expense successfully")
    void testProcessApproval_ManagerRejects_Success() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("manager.john")).thenReturn(Optional.of(manager));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseApprovalRepository.findByExpenseIdOrderByCreatedAtAsc(1L))
            .thenReturn(Arrays.asList());

        // Act
        Expense result = approvalService.processApproval(
            1L, "manager.john", ApprovalStatus.REJECTED, "Missing receipt"
        );

        // Assert
        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED, result.getApprovalStatus());
        assertNotNull(result.getRejectedAt());
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Test 3: Finance approves expense successfully")
    void testProcessApproval_FinanceApproves_Success() {
        // Arrange
        testExpense.setApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("finance.jane")).thenReturn(Optional.of(finance));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseApprovalRepository.findByExpenseIdOrderByCreatedAtAsc(1L))
            .thenReturn(Arrays.asList());

        // Act
        Expense result = approvalService.processApproval(
            1L, "finance.jane", ApprovalStatus.APPROVED, "Approved for payment"
        );

        // Assert
        assertNotNull(result);
        assertEquals(ApprovalStatus.APPROVED, result.getApprovalStatus());
        assertNotNull(result.getApprovedAt());
    }

    @Test
    @DisplayName("Test 4: Throws exception when expense not found")
    void testProcessApproval_ExpenseNotFound_ThrowsException() {
        // Arrange
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            approvalService.processApproval(
                999L, "manager.john", ApprovalStatus.APPROVED, "Comments"
            );
        });

        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 5: Throws exception when approver not found")
    void testProcessApproval_ApproverNotFound_ThrowsException() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("unknown.user")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            approvalService.processApproval(
                1L, "unknown.user", ApprovalStatus.APPROVED, "Comments"
            );
        });

        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 6: Throws exception for invalid state transition")
    void testProcessApproval_InvalidTransition_ThrowsException() {
        // Arrange
        testExpense.setApprovalStatus(ApprovalStatus.APPROVED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("manager.john")).thenReturn(Optional.of(manager));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            approvalService.processApproval(
                1L, "manager.john", ApprovalStatus.PENDING, "Invalid"
            );
        });

        assertTrue(exception.getMessage().contains("Cannot change status"));
    }

    @Test
    @DisplayName("Test 7: Get approval history successfully")
    void testGetApprovalHistory_Success() {
        // Arrange
        ExpenseApproval approval1 = new ExpenseApproval();
        approval1.setId(1L);
        approval1.setApprovalLevel("MANAGER");
        approval1.setStatus(ApprovalStatus.APPROVED);

        ExpenseApproval approval2 = new ExpenseApproval();
        approval2.setId(2L);
        approval2.setApprovalLevel("FINANCE");
        approval2.setStatus(ApprovalStatus.APPROVED);

        List<ExpenseApproval> expectedHistory = Arrays.asList(approval1, approval2);
        when(expenseApprovalRepository.findByExpenseIdOrderByCreatedAtAsc(1L))
            .thenReturn(expectedHistory);

        // Act
        List<ExpenseApproval> result = approvalService.getApprovalHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("MANAGER", result.get(0).getApprovalLevel());
        assertEquals("FINANCE", result.get(1).getApprovalLevel());
    }

    @Test
    @DisplayName("Test 8: Validates transition from PENDING to any status")
    void testValidateTransition_FromPending_AllowsAllTransitions() {
        // Arrange
        testExpense.setApprovalStatus(ApprovalStatus.PENDING);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("manager.john")).thenReturn(Optional.of(manager));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseApprovalRepository.findByExpenseIdOrderByCreatedAtAsc(1L))
            .thenReturn(Arrays.asList());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            approvalService.processApproval(
                1L, "manager.john", ApprovalStatus.MANAGER_REVIEW, "Sent to manager"
            );
        });
    }

    @Test
    @DisplayName("Test 9: Manager transitions expense to FINANCE_REVIEW with STATUS_CHANGE action")
    void testProcessApproval_ManagerReviewToFinanceReview() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("manager.john")).thenReturn(Optional.of(manager));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseApprovalRepository.findByExpenseIdOrderByCreatedAtAsc(1L))
            .thenReturn(Collections.emptyList());

        // Act
        Expense result = approvalService.processApproval(
            1L, "manager.john", ApprovalStatus.FINANCE_REVIEW, null
        );

        // Assert
        assertNotNull(result);
        assertEquals(ApprovalStatus.FINANCE_REVIEW, result.getApprovalStatus());
        assertNull(result.getApprovedAt());
        assertNull(result.getRejectedAt());
        verify(expenseRepository).save(testExpense);
        verify(expenseApprovalRepository).save(any(ExpenseApproval.class));
        verify(auditEntryRepository).save(any(AuditEntry.class));
    }

    @Test
    @DisplayName("Test 10: Updates existing pending ExpenseApproval record with comments")
    void testProcessApproval_UpdatesExistingApproval_WithComments() {
        // Arrange
        ExpenseApproval existingApproval = new ExpenseApproval();
        existingApproval.setId(10L);
        existingApproval.setStatus(ApprovalStatus.MANAGER_REVIEW);
        existingApproval.setApprovalLevel("MANAGER");

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("manager.john")).thenReturn(Optional.of(manager));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseApprovalRepository.findByExpenseIdOrderByCreatedAtAsc(1L))
            .thenReturn(Collections.singletonList(existingApproval));

        // Act
        approvalService.processApproval(
            1L, "manager.john", ApprovalStatus.APPROVED, "Updated comments"
        );

        // Assert
        assertEquals(ApprovalStatus.APPROVED, existingApproval.getStatus());
        assertEquals(manager, existingApproval.getApprover());
        assertEquals("Updated comments", existingApproval.getComments());
        verify(expenseApprovalRepository).save(existingApproval);
    }

    @Test
    @DisplayName("Test 11: Updates existing pending ExpenseApproval record without comments")
    void testProcessApproval_UpdatesExistingApproval_WithoutComments() {
        // Arrange
        ExpenseApproval existingApproval = new ExpenseApproval();
        existingApproval.setId(10L);
        existingApproval.setStatus(ApprovalStatus.MANAGER_REVIEW);
        existingApproval.setApprovalLevel("MANAGER");
        existingApproval.setComments("Existing comment");

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("manager.john")).thenReturn(Optional.of(manager));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseApprovalRepository.findByExpenseIdOrderByCreatedAtAsc(1L))
            .thenReturn(Collections.singletonList(existingApproval));

        // Act
        approvalService.processApproval(
            1L, "manager.john", ApprovalStatus.APPROVED, null
        );

        // Assert
        assertEquals(ApprovalStatus.APPROVED, existingApproval.getStatus());
        assertEquals("Existing comment", existingApproval.getComments());
        verify(expenseApprovalRepository).save(existingApproval);
    }

    @Test
    @DisplayName("Test 12: Finance rejects expense successfully")
    void testProcessApproval_FinanceRejects_Success() {
        // Arrange
        testExpense.setApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("finance.jane")).thenReturn(Optional.of(finance));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseApprovalRepository.findByExpenseIdOrderByCreatedAtAsc(1L))
            .thenReturn(Collections.emptyList());

        // Act
        Expense result = approvalService.processApproval(
            1L, "finance.jane", ApprovalStatus.REJECTED, "Budget exceeded"
        );

        // Assert
        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED, result.getApprovalStatus());
        assertNotNull(result.getRejectedAt());
    }

    @Test
    @DisplayName("Test 13: Throws exception for invalid transition from MANAGER_REVIEW")
    void testValidateTransition_FromManagerReview_InvalidTransition() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("manager.john")).thenReturn(Optional.of(manager));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            approvalService.processApproval(
                1L, "manager.john", ApprovalStatus.PENDING, "Invalid"
            );
        });
        assertTrue(ex.getMessage().contains("Invalid transition from MANAGER_REVIEW"));
    }

    @Test
    @DisplayName("Test 14: Throws exception for invalid transition from FINANCE_REVIEW")
    void testValidateTransition_FromFinanceReview_InvalidTransition() {
        // Arrange
        testExpense.setApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("finance.jane")).thenReturn(Optional.of(finance));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            approvalService.processApproval(
                1L, "finance.jane", ApprovalStatus.MANAGER_REVIEW, "Invalid"
            );
        });
        assertTrue(ex.getMessage().contains("Invalid transition from FINANCE_REVIEW"));
    }

    @Test
    @DisplayName("Test 15: Throws exception when attempting transition on AUTO_APPROVED expense")
    void testValidateTransition_FromAutoApproved_ThrowsException() {
        // Arrange
        testExpense.setApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("manager.john")).thenReturn(Optional.of(manager));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            approvalService.processApproval(
                1L, "manager.john", ApprovalStatus.APPROVED, "Invalid"
            );
        });
        assertTrue(ex.getMessage().contains("Cannot change status of expense in AUTO_APPROVED state"));
    }

    @Test
    @DisplayName("Test 16: Throws exception when attempting transition on REJECTED expense")
    void testValidateTransition_FromRejected_ThrowsException() {
        // Arrange
        testExpense.setApprovalStatus(ApprovalStatus.REJECTED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("manager.john")).thenReturn(Optional.of(manager));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            approvalService.processApproval(
                1L, "manager.john", ApprovalStatus.APPROVED, "Invalid"
            );
        });
        assertTrue(ex.getMessage().contains("Cannot change status of expense in REJECTED state"));
    }
}