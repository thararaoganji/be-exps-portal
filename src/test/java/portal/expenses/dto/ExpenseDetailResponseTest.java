package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.Expense;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ExpenseDetailResponseTest {

    @Test
    void testGettersAndSetters() {
        ExpenseDetailResponse response = new ExpenseDetailResponse();

        assertNull(response.getExpense());
        assertNull(response.getApprovals());

        Expense expense = new Expense();
        response.setExpense(expense);
        assertSame(expense, response.getExpense());

        ExpenseApprovalResponse approval = new ExpenseApprovalResponse();
        List<ExpenseApprovalResponse> approvals = Collections.singletonList(approval);
        response.setApprovals(approvals);
        assertEquals(approvals, response.getApprovals());
    }
}