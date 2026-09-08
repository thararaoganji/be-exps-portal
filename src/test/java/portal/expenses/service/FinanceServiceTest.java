package portal.expenses.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private FinanceService financeService;

    @Test
    void markAsReimbursed_success() {
        Expense expense = mock(Expense.class);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense result = financeService.markAsReimbursed(1L);

        assertNotNull(result);
        verify(expense).setApprovalStatus(ApprovalStatus.REIMBURSED);
        verify(expenseRepository).findById(1L);
        verify(expenseRepository).save(expense);
    }

    @Test
    void markAsReimbursed_notFound_throwsException() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                financeService.markAsReimbursed(99L)
        );

        assertTrue(exception.getMessage().contains("Expense not found with ID: 99"));
    }

    @Test
    void markAsReimbursed_notApproved_throwsIllegalStateException() {
        Expense expense = mock(Expense.class);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                financeService.markAsReimbursed(1L)
        );

        assertTrue(exception.getMessage().contains("Only approved expenses can be marked as reimbursed"));
    }

    @Test
    void exportReimbursedExpensesToCsv_withDateRange() {
        Expense expense = mock(Expense.class);
        AppUser user = mock(AppUser.class);

        when(user.getUsername()).thenReturn("testuser");
        when(expense.getId()).thenReturn(1L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn("Team lunch");
        when(expense.getAmount()).thenReturn(new BigDecimal("50.00"));
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 5, 10));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(true);

        LocalDate start = LocalDate.of(2023, 5, 1);
        LocalDate end = LocalDate.of(2023, 5, 31);

        when(expenseRepository.findByApprovalStatusAndExpenseDateBetween(ApprovalStatus.REIMBURSED, start, end))
                .thenReturn(List.of(expense));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv("2023-05-01", "2023-05-31");
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt"));
        assertTrue(csvContent.contains("1,testuser,Team lunch,50.00,"));
        assertTrue(csvContent.contains("2023-05-10,"));
        assertTrue(csvContent.contains("REIMBURSED,true"));
    }

    @Test
    void exportReimbursedExpensesToCsv_withoutDateRange() {
        Expense expense = mock(Expense.class);
        AppUser user = mock(AppUser.class);

        when(user.getUsername()).thenReturn("testuser");
        when(expense.getId()).thenReturn(1L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn("Team lunch");
        when(expense.getAmount()).thenReturn(new BigDecimal("50.00"));
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 5, 10));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(true);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(List.of(expense));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(null, null);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt"));
        assertTrue(csvContent.contains("1,testuser,Team lunch,50.00,"));
    }

    @Test
    void exportReimbursedExpensesToCsv_withSpecialCharactersEscaped() {
        Expense expense = mock(Expense.class);
        AppUser user = mock(AppUser.class);

        when(user.getUsername()).thenReturn("user,with,comma");
        when(expense.getId()).thenReturn(2L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn("Description with \"quotes\" and \nnewline");
        when(expense.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 5, 15));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(false);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(List.of(expense));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(null, null);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("\"user,with,comma\""));
        assertTrue(csvContent.contains("\"Description with \"\"quotes\"\" and \nnewline\""));
    }

    @Test
    void exportReimbursedExpensesToCsv_withNullFields() {
        Expense expense = mock(Expense.class);
        AppUser user = mock(AppUser.class);

        when(user.getUsername()).thenReturn(null);
        when(expense.getId()).thenReturn(3L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn(null);
        when(expense.getAmount()).thenReturn(new BigDecimal("10.00"));
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 5, 20));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(true);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(List.of(expense));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(null, null);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("3,,,10.00,"));
    }
}