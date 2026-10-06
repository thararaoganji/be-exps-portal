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
    void markAsReimbursed_Success() {
        Long expenseId = 1L;
        Expense expense = mock(Expense.class);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = financeService.markAsReimbursed(expenseId);

        assertNotNull(result);
        verify(expense).setApprovalStatus(ApprovalStatus.REIMBURSED);
        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository).save(expense);
    }

    @Test
    void markAsReimbursed_NotFound_ThrowsException() {
        Long expenseId = 999L;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                financeService.markAsReimbursed(expenseId));

        assertEquals("Expense not found with ID: " + expenseId, exception.getMessage());
    }

    @Test
    void markAsReimbursed_NotApproved_ThrowsIllegalStateException() {
        Long expenseId = 1L;
        Expense expense = mock(Expense.class);
        when(expense.getApprovalStatus()).thenReturn(null);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                financeService.markAsReimbursed(expenseId));

        assertTrue(exception.getMessage().contains("Only approved expenses can be marked as reimbursed. Current status: null"));
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
        when(expense.getDescription()).thenReturn("Client lunch");
        when(expense.getAmount()).thenReturn(null);
        when(expense.getCategory()).thenReturn(null);
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 1, 15));
        when(expense.getCreatedAt()).thenReturn(null);
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(true);

        when(expenseRepository.findByApprovalStatusAndExpenseDateBetween(
                ApprovalStatus.REIMBURSED, startDate, endDate))
                .thenReturn(List.of(expense));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(startDateStr, endDateStr);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        String expectedHeader = "Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n";
        String expectedRow = "10,john_doe,Client lunch,null,null,2023-01-15,null,REIMBURSED,true\n";

        assertEquals(expectedHeader + expectedRow, csvContent);
    }

    @Test
    void exportReimbursedExpensesToCsv_WithoutDateRange_AndEscapedFields() {
        AppUser user1 = mock(AppUser.class);
        when(user1.getUsername()).thenReturn("jane,\"the boss\",doe");

        Expense expense1 = mock(Expense.class);
        when(expense1.getId()).thenReturn(1L);
        when(expense1.getUser()).thenReturn(user1);
        when(expense1.getDescription()).thenReturn("Flight\nand hotel, premium");
        when(expense1.getAmount()).thenReturn(null);
        when(expense1.getCategory()).thenReturn(null);
        when(expense1.getExpenseDate()).thenReturn(LocalDate.of(2023, 2, 10));
        when(expense1.getCreatedAt()).thenReturn(null);
        when(expense1.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense1.getHasReceipt()).thenReturn(false);

        AppUser user2 = mock(AppUser.class);
        when(user2.getUsername()).thenReturn(null);

        Expense expense2 = mock(Expense.class);
        when(expense2.getId()).thenReturn(2L);
        when(expense2.getUser()).thenReturn(user2);
        when(expense2.getDescription()).thenReturn(null);
        when(expense2.getAmount()).thenReturn(null);
        when(expense2.getCategory()).thenReturn(null);
        when(expense2.getExpenseDate()).thenReturn(LocalDate.of(2023, 2, 12));
        when(expense2.getCreatedAt()).thenReturn(null);
        when(expense2.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense2.getHasReceipt()).thenReturn(true);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(List.of(expense1, expense2));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(null, null);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        String expectedHeader = "Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n";
        String expectedRow1 = "1,\"jane,\"\"the boss\"\",doe\",\"Flight\nand hotel, premium\",null,null,2023-02-10,null,REIMBURSED,false\n";
        String expectedRow2 = "2,,,null,null,2023-02-12,null,REIMBURSED,true\n";

        assertEquals(expectedHeader + expectedRow1 + expectedRow2, csvContent);
    }

    @Test
    void exportReimbursedExpensesToCsv_OneDateNull_UsesFindByApprovalStatus() {
        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(Collections.emptyList());

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv("2023-01-01", null);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        String expectedHeader = "Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n";
        assertEquals(expectedHeader, csvContent);
        verify(expenseRepository).findByApprovalStatus(ApprovalStatus.REIMBURSED);
    }
}