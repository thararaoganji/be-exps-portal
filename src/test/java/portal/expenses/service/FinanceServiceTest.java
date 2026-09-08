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
import java.util.Collections;
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
    void markAsReimbursed_success() {
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
    void markAsReimbursed_notFound_throwsRuntimeException() {
        Long expenseId = 1L;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> financeService.markAsReimbursed(expenseId));

        assertTrue(exception.getMessage().contains("Expense not found with ID: 1"));
    }

    @Test
    void markAsReimbursed_notApproved_throwsIllegalStateException() {
        Long expenseId = 1L;
        Expense expense = mock(Expense.class);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REJECTED);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> financeService.markAsReimbursed(expenseId));

        assertTrue(exception.getMessage().contains("Only approved expenses can be marked as reimbursed. Current status: REJECTED"));
    }

    @Test
    void exportReimbursedExpensesToCsv_withDates() {
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-01-31";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        AppUser user = mock(AppUser.class);
        when(user.getUsername()).thenReturn("john,doe");

        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(10L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn("Lunch \"meeting\"\nwith client");
        when(expense.getAmount()).thenReturn(null);
        when(expense.getCategory()).thenReturn(null);
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 1, 15));
        when(expense.getCreatedAt()).thenReturn(null);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(true);

        when(expenseRepository.findByApprovalStatusAndExpenseDateBetween(
                ApprovalStatus.REIMBURSED, startDate, endDate))
                .thenReturn(List.of(expense));

        byte[] result = financeService.exportReimbursedExpensesToCsv(startDateStr, endDateStr);

        String csvString = new String(result, StandardCharsets.UTF_8);
        assertTrue(csvString.startsWith("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n"));
        assertTrue(csvString.contains("10,\"john,doe\",\"Lunch \"\"meeting\"\"\nwith client\",null,null,2023-01-15,null,REIMBURSED,true\n"));
    }

    @Test
    void exportReimbursedExpensesToCsv_withoutDates_handlesNullFields() {
        AppUser user = mock(AppUser.class);
        when(user.getUsername()).thenReturn(null);

        Expense expense = mock(Expense.class);
        when(expense.getId()).thenReturn(20L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn("Plain description");
        when(expense.getAmount()).thenReturn(null);
        when(expense.getCategory()).thenReturn(null);
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 2, 1));
        when(expense.getCreatedAt()).thenReturn(null);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(false);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(List.of(expense));

        byte[] result = financeService.exportReimbursedExpensesToCsv(null, null);

        String csvString = new String(result, StandardCharsets.UTF_8);
        assertTrue(csvString.contains("20,,Plain description,null,null,2023-02-01,null,REIMBURSED,false\n"));
    }

    @Test
    void exportReimbursedExpensesToCsv_partialDates_treatedAsWithoutDates() {
        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(Collections.emptyList());

        byte[] result = financeService.exportReimbursedExpensesToCsv("2023-01-01", null);

        String csvString = new String(result, StandardCharsets.UTF_8);
        assertEquals("Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n", csvString);
    }
}