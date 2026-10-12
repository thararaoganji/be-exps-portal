package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private FinanceService financeService;

    private Expense expense;
    private AppUser user;

    @BeforeEach
    void setUp() {
        user = mock(AppUser.class);
        expense = mock(Expense.class);
    }

    @Test
    void markAsReimbursed_Success() {
        Long expenseId = 1L;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense result = financeService.markAsReimbursed(expenseId);

        assertNotNull(result);
        verify(expense).setApprovalStatus(ApprovalStatus.REIMBURSED);
        verify(expenseRepository).save(expense);
    }

    @Test
    void markAsReimbursed_NotFound_ThrowsRuntimeException() {
        Long expenseId = 99L;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                financeService.markAsReimbursed(expenseId)
        );

        assertEquals("Expense not found with ID: " + expenseId, exception.getMessage());
    }

    @Test
    void markAsReimbursed_NotApproved_ThrowsIllegalStateException() {
        Long expenseId = 1L;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                financeService.markAsReimbursed(expenseId)
        );

        assertEquals("Only approved expenses can be marked as reimbursed. Current status: PENDING", exception.getMessage());
    }

    @Test
    void exportReimbursedExpensesToCsv_WithDateRange() {
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-01-31";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        when(user.getUsername()).thenReturn("john_doe");
        when(expense.getId()).thenReturn(10L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn("Flight, with \"quotes\" & \nnewlines");
        when(expense.getAmount()).thenReturn(BigDecimal.valueOf(150.50));
        when(expense.getCategory()).thenReturn(null);
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 1, 15));
        when(expense.getCreatedAt()).thenReturn(LocalDateTime.of(2023, 1, 16, 10, 0));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(Boolean.TRUE);

        when(expenseRepository.findByApprovalStatusAndExpenseDateBetween(
                ApprovalStatus.REIMBURSED, startDate, endDate
        )).thenReturn(List.of(expense));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(startDateStr, endDateStr);

        String csvString = new String(csvBytes, StandardCharsets.UTF_8);
        String expectedHeader = "Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n";
        String expectedRow = "10,john_doe,\"Flight, with \"\"quotes\"\" & \nnewlines\",150.5,null,2023-01-15,2023-01-16T10:00,REIMBURSED,true\n";

        assertEquals(expectedHeader + expectedRow, csvString);
    }

    @Test
    void exportReimbursedExpensesToCsv_WithoutDates_AndNullFieldHandling() {
        when(user.getUsername()).thenReturn(null);
        when(expense.getId()).thenReturn(20L);
        when(expense.getUser()).thenReturn(user);
        when(expense.getDescription()).thenReturn(null);
        when(expense.getAmount()).thenReturn(BigDecimal.valueOf(50.0));
        when(expense.getCategory()).thenReturn(null);
        when(expense.getExpenseDate()).thenReturn(LocalDate.of(2023, 2, 1));
        when(expense.getCreatedAt()).thenReturn(LocalDateTime.of(2023, 2, 2, 8, 30));
        when(expense.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expense.getHasReceipt()).thenReturn(Boolean.FALSE);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(List.of(expense));

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv(null, null);

        String csvString = new String(csvBytes, StandardCharsets.UTF_8);
        String expectedHeader = "Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n";
        String expectedRow = "20,,,50.0,null,2023-02-01,2023-02-02T08:30,REIMBURSED,false\n";

        assertEquals(expectedHeader + expectedRow, csvString);
    }

    @Test
    void exportReimbursedExpensesToCsv_OneDateNull_FallsBackToAllReimbursed() {
        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(Collections.emptyList());

        byte[] csvBytes = financeService.exportReimbursedExpensesToCsv("2023-01-01", null);

        String csvString = new String(csvBytes, StandardCharsets.UTF_8);
        String expectedHeader = "Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n";

        assertEquals(expectedHeader, csvString);
    }
}