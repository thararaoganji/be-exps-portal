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
        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setApprovalStatus(ApprovalStatus.APPROVED);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = financeService.markAsReimbursed(expenseId);

        assertNotNull(result);
        assertEquals(ApprovalStatus.REIMBURSED, result.getApprovalStatus());
        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository).save(expense);
    }

    @Test
    void markAsReimbursed_ExpenseNotFound() {
        Long expenseId = 1L;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                financeService.markAsReimbursed(expenseId)
        );

        assertTrue(exception.getMessage().contains("Expense not found with ID: 1"));
    }

    @Test
    void markAsReimbursed_InvalidStatus() {
        Long expenseId = 1L;
        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setApprovalStatus(ApprovalStatus.PENDING);

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

        Expense expense = mock(Expense.class);
        AppUser user = mock(AppUser.class);
        when(user.getUsername()).thenReturn("john_doe");
        when(expense.getId()).thenReturn(10L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn("Team lunch");
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 1, 15));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(true);

        when(expenseRepository.findByApprovalStatusAndExpenseDateBetween(ApprovalStatus.REIMBURSED, startDate, endDate))
                .thenReturn(List.of(expense));

        byte[] result = financeService.exportReimbursedExpensesToCsv(startDateStr, endDateStr);

        assertNotNull(result);
        String csvContent = new String(result, StandardCharsets.UTF_8);
        assertTrue(csvContent.startsWith("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n"));
        assertTrue(csvContent.contains("10,john_doe,Team lunch"));
    }

    @Test
    void exportReimbursedExpensesToCsv_WithoutDateRange() {
        Expense expense1 = mock(Expense.class);
        AppUser user1 = mock(AppUser.class);
        when(user1.getUsername()).thenReturn("jane,smith"); // test comma escaping
        when(expense1.getId()).thenReturn(11L);
        when(expense1.getUser()).thenReturn(user1);
        when(expense1.getDescription()).thenReturn("Flight \"ticket\"\nwith luggage"); // test quotes and newlines escaping
        when(expense1.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense1.getHasReceipt()).thenReturn(false);

        Expense expense2 = mock(Expense.class);
        AppUser user2 = mock(AppUser.class);
        when(user2.getUsername()).thenReturn(null); // test null field escaping
        when(expense2.getId()).thenReturn(12L);
        when(expense2.getUser()).thenReturn(user2);
        when(expense2.getDescription()).thenReturn(null);
        when(expense2.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense2.getHasReceipt()).thenReturn(true);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(List.of(expense1, expense2));

        byte[] result = financeService.exportReimbursedExpensesToCsv(null, null);

        assertNotNull(result);
        String csvContent = new String(result, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("\"jane,smith\",\"Flight \"\"ticket\"\"\nwith luggage\""));
        assertTrue(csvContent.contains("12,,,"));
    }
}