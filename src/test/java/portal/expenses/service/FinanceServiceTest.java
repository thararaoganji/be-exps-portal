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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private Expense expenseMock1;

    @Mock
    private Expense expenseMock2;

    @Mock
    private AppUser userMock1;

    @Mock
    private AppUser userMock2;

    private FinanceService financeService;

    @BeforeEach
    void setUp() {
        financeService = new FinanceService(expenseRepository);
    }

    @Test
    void markAsReimbursed_Success() {
        Long expenseId = 1L;
        when(expenseMock1.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseMock1));
        when(expenseRepository.save(expenseMock1)).thenReturn(expenseMock1);

        Expense result = financeService.markAsReimbursed(expenseId);

        assertNotNull(result);
        verify(expenseMock1).setApprovalStatus(ApprovalStatus.REIMBURSED);
        verify(expenseRepository).save(expenseMock1);
    }

    @Test
    void markAsReimbursed_NotFound_ThrowsException() {
        Long expenseId = 99L;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> financeService.markAsReimbursed(expenseId));
        assertEquals("Expense not found with ID: " + expenseId, exception.getMessage());
    }

    @Test
    void markAsReimbursed_NotApproved_ThrowsException() {
        Long expenseId = 1L;
        when(expenseMock1.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseMock1));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> financeService.markAsReimbursed(expenseId));
        assertEquals("Only approved expenses can be marked as reimbursed. Current status: REIMBURSED",
                exception.getMessage());
    }

    @Test
    void exportReimbursedExpensesToCsv_WithoutDates() {
        LocalDate expenseDate = LocalDate.of(2023, 10, 15);
        LocalDateTime createdAt = LocalDateTime.of(2023, 10, 16, 12, 0);

        when(userMock1.getUsername()).thenReturn("john_doe");
        when(expenseMock1.getId()).thenReturn(1L);
        when(expenseMock1.getUser()).thenReturn(userMock1);
        when(expenseMock1.getDescription()).thenReturn("Flight ticket");
        when(expenseMock1.getAmount()).thenReturn(new BigDecimal("150.00"));
        when(expenseMock1.getExpenseDate()).thenReturn(expenseDate);
        when(expenseMock1.getCreatedAt()).thenReturn(createdAt);
        when(expenseMock1.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expenseMock1.getHasReceipt()).thenReturn(Boolean.TRUE);

        when(expenseRepository.findByApprovalStatus(ApprovalStatus.REIMBURSED))
                .thenReturn(Collections.singletonList(expenseMock1));

        byte[] result = financeService.exportReimbursedExpensesToCsv(null, null);

        String expectedCsv = "Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n" +
                "1,john_doe,Flight ticket,150.00,null,2023-10-15,2023-10-16T12:00,REIMBURSED,true\n";

        assertArrayEquals(expectedCsv.getBytes(StandardCharsets.UTF_8), result);
    }

    @Test
    void exportReimbursedExpensesToCsv_WithDatesAndEscapedFields() {
        String startDateStr = "2023-10-01";
        String endDateStr = "2023-10-31";
        LocalDate startDate = LocalDate.of(2023, 10, 1);
        LocalDate endDate = LocalDate.of(2023, 10, 31);
        LocalDate expenseDate = LocalDate.of(2023, 10, 10);
        LocalDateTime createdAt = LocalDateTime.of(2023, 10, 11, 9, 30);

        // First expense with quotes, commas, and newlines in text fields
        when(userMock1.getUsername()).thenReturn("Doe, \"Johnny\"");
        when(expenseMock1.getId()).thenReturn(1L);
        when(expenseMock1.getUser()).thenReturn(userMock1);
        when(expenseMock1.getDescription()).thenReturn("Lunch\nDinner");
        when(expenseMock1.getAmount()).thenReturn(new BigDecimal("75.50"));
        when(expenseMock1.getExpenseDate()).thenReturn(expenseDate);
        when(expenseMock1.getCreatedAt()).thenReturn(createdAt);
        when(expenseMock1.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expenseMock1.getHasReceipt()).thenReturn(Boolean.FALSE);

        // Second expense with null fields
        when(userMock2.getUsername()).thenReturn(null);
        when(expenseMock2.getId()).thenReturn(2L);
        when(expenseMock2.getUser()).thenReturn(userMock2);
        when(expenseMock2.getDescription()).thenReturn(null);
        when(expenseMock2.getAmount()).thenReturn(new BigDecimal("20.00"));
        when(expenseMock2.getExpenseDate()).thenReturn(expenseDate);
        when(expenseMock2.getCreatedAt()).thenReturn(createdAt);
        when(expenseMock2.getApprovalStatus()).thenReturn(ApprovalStatus.REIMBURSED);
        when(expenseMock2.getHasReceipt()).thenReturn(Boolean.TRUE);

        when(expenseRepository.findByApprovalStatusAndExpenseDateBetween(
                ApprovalStatus.REIMBURSED, startDate, endDate))
                .thenReturn(Arrays.asList(expenseMock1, expenseMock2));

        byte[] result = financeService.exportReimbursedExpensesToCsv(startDateStr, endDateStr);

        String expectedCsv = "Expense ID,Employee,Description,Amount,Category,Expense Date,Submitted Date,Status,Has Receipt\n" +
                "1,\"Doe, \"\"Johnny\"\"\",\"Lunch\nDinner\",75.50,null,2023-10-10,2023-10-11T09:30,REIMBURSED,false\n" +
                "2,,,20.00,null,2023-10-10,2023-10-11T09:30,REIMBURSED,true\n";

        assertArrayEquals(expectedCsv.getBytes(StandardCharsets.UTF_8), result);
    }
}