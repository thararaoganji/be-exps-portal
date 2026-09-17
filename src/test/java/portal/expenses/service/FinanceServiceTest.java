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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void markAsReimbursed_whenExpenseNotFound_throwsRuntimeException() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                financeService.markAsReimbursed(1L)
        );

        assertTrue(exception.getMessage().contains("Expense not found with ID: 1"));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void markAsReimbursed_whenExpenseNotApproved_throwsIllegalStateException() {
        Expense expense = mock(Expense.class);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                financeService.markAsReimbursed(1L)
        );

        assertTrue(exception.getMessage().contains("Only approved expenses can be marked as reimbursed"));
        verify(expense, never()).setApprovalStatus(any());
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void markAsReimbursed_whenExpenseApproved_success() {
        Expense expense = mock(Expense.class);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense result = financeService.markAsReimbursed(1L);

        assertNotNull(result);
        verify(expense).setApprovalStatus(ApprovalStatus.REIMBURSED);
        verify(expenseRepository).save(expense);
    }

    @Test
    void exportReimbursedExpensesToCsv_withDateRange_returnsCsvBytes() {
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-01-31";
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        AppUser user = mock(AppUser.class);
        when(user.getUsername()).thenReturn("john_doe");

        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(100L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn("Office \"supplies\", and pens\nnext line");
        when(expense.getAmount()).thenReturn(new BigDecimal("150.50"));
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 1, 15));
        when(expense.getCreatedAt()).thenReturn(LocalDateTime.of(2023, 1, 16, 10, 0));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(true);

        when(expenseRepository.findByApprovalStatusAndExpenseDateBetween(
                ApprovalStatus.REIMBURSED, startDate, endDate))
                .thenReturn(List.of(expense));

        byte[] resultBytes = financeService.exportReimbursedExpensesToCsv(startDateStr, endDateStr);
        String csvOutput = new String(resultBytes, StandardCharsets.UTF_8);

        assertTrue(csvOutput.startsWith("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n"));
        assertTrue(csvOutput.contains("100,john_doe,\"Office \"\"supplies\"\", and pens\nnext line\",150.50,null,2023-01-15,2023-01-16T10:00,REIMBURSED,true\n"));
    }

    @Test
    void exportReimbursedExpensesToCsv_withoutDateRange_returnsCsvBytes() {
        AppUser user = mock(AppUser.class);
        when(user.getUsername()).thenReturn(null);

        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(101L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn(null);
        when(expense.getAmount()).thenReturn(new BigDecimal("50.00"));
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 2, 1));
        when(expense.getCreatedAt()).thenReturn(LocalDateTime.of(2023, 2, 2, 11, 0));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(false);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(List.of(expense));

        byte[] resultBytes = financeService.exportReimbursedExpensesToCsv(null, null);
        String csvOutput = new String(resultBytes, StandardCharsets.UTF_8);

        assertTrue(csvOutput.startsWith("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n"));
        assertTrue(csvOutput.contains("101,,,50.00,null,2023-02-01,2023-02-02T11:00,REIMBURSED,false\n"));
    }

    @Test
    void exportReimbursedExpensesToCsv_emptyList() {
        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(Collections.emptyList());

        byte[] resultBytes = financeService.exportReimbursedExpensesToCsv(null, "2023-01-31");
        String csvOutput = new String(resultBytes, StandardCharsets.UTF_8);

        assertEquals("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n", csvOutput);
    }
}