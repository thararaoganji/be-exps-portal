package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portal.expenses.entity.AppUser;
import portal.expenses.entity.ApprovalStatus;
import portal.expenses.entity.Expense;
import portal.expenses.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    private FinanceService financeService;

    @BeforeEach
    void setUp() {
        financeService = new FinanceService(expenseRepository);
    }

    @Test
    void markAsReimbursed_Success() {
        Long expenseId = 1L;
        Expense expense = mock(Expense.class);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense result = financeService.markAsReimbursed(expenseId);

        assertNotNull(result);
        verify(expense).setApprovalStatus(ApprovalStatus.REIMBURSED);
        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository).save(expense);
    }

    @Test
    void markAsReimbursed_ExpenseNotFound_ThrowsException() {
        Long expenseId = 999L;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                financeService.markAsReimbursed(expenseId)
        );

        assertTrue(exception.getMessage().contains("Expense not found with ID: 999"));
    }

    @Test
    void markAsReimbursed_NotApproved_ThrowsIllegalStateException() {
        Long expenseId = 1L;
        Expense expense = mock(Expense.class);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                financeService.markAsReimbursed(expenseId)
        );

        assertTrue(exception.getMessage().contains("Only approved expenses can be marked as reimbursed"));
    }

    @Test
    void exportReimbursedExpensesToCsv_WithDateRange() {
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-01-31";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        AppUser user = mock(AppUser.class);
        when(user.getUsername()).thenReturn("john_doe");

        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(10L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn("Travel, \"flight\"\nand hotel");
        when(expense.getAmount()).thenReturn(new BigDecimal("150.00"));
        when(expense.getCategory()).thenReturn(null);
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 1, 15));
        when(expense.getCreatedAt()).thenReturn(LocalDateTime.of(2023, 1, 16, 10, 0));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(true);

        when(expenseRepository.findByApprovalStatusAndExpenseDateBetween(
                ApprovalStatus.REIMBURSED, startDate, endDate)).thenReturn(List.of(expense));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(startDateStr, endDateStr);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.startsWith("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n"));
        assertTrue(csvContent.contains("10,john_doe,\"Travel, \"\"flight\"\"\nand hotel\",150.00,null,2023-01-15,2023-01-16T10:00,REIMBURSED,true"));
    }

    @Test
    void exportReimbursedExpensesToCsv_WithoutDateRange_AndNullFields() {
        AppUser user = mock(AppUser.class);
        when(user.getUsername()).thenReturn(null);

        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(20L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn(null);
        when(expense.getAmount()).thenReturn(new BigDecimal("50.00"));
        when(expense.getCategory()).thenReturn(null);
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 2, 1));
        when(expense.getCreatedAt()).thenReturn(LocalDateTime.of(2023, 2, 2, 12, 0));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(false);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED)).thenReturn(List.of(expense));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(null, null);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.startsWith("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n"));
        assertTrue(csvContent.contains("20,,,50.00,null,2023-02-01,2023-02-02T12:00,REIMBURSED,false"));
    }
}