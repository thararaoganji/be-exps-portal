package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PolicyRuleTest {

    @Test
    void testGettersAndSetters() {
        PolicyRule rule = new PolicyRule();

        rule.setId(10L);
        assertEquals(10L, rule.getId());

        rule.setRuleName("Rule 1");
        assertEquals("Rule 1", rule.getRuleName());

        rule.setRuleType(PolicyRuleType.AUTO_APPROVE);
        assertEquals(PolicyRuleType.AUTO_APPROVE, rule.getRuleType());

        rule.setEnabled(false);
        assertFalse(rule.getEnabled());

        rule.setPriority(5);
        assertEquals(5, rule.getPriority());

        rule.setConditionCategory(ExpenseCategory.TRAVEL);
        assertEquals(ExpenseCategory.TRAVEL, rule.getConditionCategory());

        BigDecimal min = new BigDecimal("10.00");
        rule.setConditionAmountMin(min);
        assertEquals(min, rule.getConditionAmountMin());

        BigDecimal max = new BigDecimal("100.00");
        rule.setConditionAmountMax(max);
        assertEquals(max, rule.getConditionAmountMax());

        rule.setConditionAgeDays(30);
        assertEquals(30, rule.getConditionAgeDays());

        rule.setConditionReceiptRequired(true);
        assertTrue(rule.getConditionReceiptRequired());

        rule.setActionApprovalStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, rule.getActionApprovalStatus());

        rule.setActionReason("Auto-approved reason");
        assertEquals("Auto-approved reason", rule.getActionReason());
    }

    @Test
    void testDefaultValues() {
        PolicyRule rule = new PolicyRule();
        assertTrue(rule.getEnabled());
        assertEquals(0, rule.getPriority());
        assertNull(rule.getId());
        assertNull(rule.getCreatedAt());
        assertNull(rule.getUpdatedAt());
    }

    @Test
    void testLifecycleHooks() {
        PolicyRule rule = new PolicyRule();

        rule.onCreate();
        assertNotNull(rule.getCreatedAt());
        assertNotNull(rule.getUpdatedAt());

        rule.onUpdate();
        assertNotNull(rule.getUpdatedAt());
    }
}