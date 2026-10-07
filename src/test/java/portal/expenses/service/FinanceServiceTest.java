package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portal.expenses.entity.ApprovalStatus;
import portal.expenses.entity.Expense;
import portal.expenses.repository.ExpenseRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = financeService.markAsReimbursed(expenseId);

        assertNotNull(result);
        verify(expense).setApprovalStatus(ApprovalStatus.REIMBURSED);
        verify(expenseRepository).save(expense);
    }

    @Test
    void markAsReimbursed_ExpenseNotFound() {
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
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                financeService.markAsReimbursed(expenseId)
        );

        assertTrue(exception.getMessage().contains("Only approved expenses can be marked as reimbursed"));
    }

    @Test
    void exportReimbursedExpensesToCsv_WithDates_EscapingChecks() {
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-01-31";
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        Expense expense1 = mock(Expense.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(expense1.getId()).thenReturn(101L);
        when(expense1.getUser().getUsername()).thenReturn("john,doe");
        when(expense1.getDescription()).thenReturn("Flight, \"Hotel\"\nDinner");
        when(expense1.getAmount()).thenReturn(null);
        when(expense1.getCategory()).thenReturn(null);
        when(expense1.getExpenseDate()).thenReturn(LocalDate.of(2023, 1, 15));
        when(expense1.getCreatedAt()).thenReturn(null);
        when(expense1.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense1.getHasReceipt()).thenReturn(Boolean.TRUE);

        when(expenseRepository.findByApprovalStatusAndExpenseDateBetween(ApprovalStatus.REIMBURSED, startDate, endDate))
                .thenReturn(List.of(expense1));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(startDateStr, endDateStr);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt"));
        assertTrue(csv.contains("101,\"john,doe\",\"Flight, \"\"Hotel\"\"\nDinner\",null,null,2023-01-15,null,REIMBURSED,true"));
    }

    @Test
    void exportReimbursedExpensesToCsv_WithoutDates_PlainAndNullFields() {
        Expense expense2 = mock(Expense.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(expense2.getId()).thenReturn(102L);
        when(expense2.getUser().getUsername()).thenReturn(null);
        when(expense2.getDescription()).thenReturn("Simple description");
        when(expense2.getAmount()).thenReturn(null);
        when(expense2.getCategory()).thenReturn(null);
        when(expense2.getExpenseDate()).thenReturn(LocalDate.of(2023, 2, 1));
        when(expense2.getCreatedAt()).thenReturn(null);
        when(expense2.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense2.getHasReceipt()).thenReturn(Boolean.FALSE);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(List.of(expense2));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(null, null);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt"));
        assertTrue(csv.contains("102,,Simple description,null,null,2023-02-01,null,REIMBURSED,false"));
    }
}