package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.Expense;

import java.util.ArrayList;
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
        List<ExpenseApprovalResponse> approvals = new ArrayList<>();
        approvals.add(new ExpenseApprovalResponse());

        response.setExpense(expense);
        response.setApprovals(approvals);

        assertSame(expense, response.getExpense());
        assertSame(approvals, response.getApprovals());
        assertEquals(1, response.getApprovals().size());
    }
}