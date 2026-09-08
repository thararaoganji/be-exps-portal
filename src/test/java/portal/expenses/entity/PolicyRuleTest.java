package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PolicyRuleTest {

    @Test
    void testGettersAndSetters() {
        PolicyRule rule = new PolicyRule();

        // Check defaults
        assertTrue(rule.getEnabled());
        assertEquals(0, rule.getPriority());
        assertNull(rule.getId());
        assertNull(rule.getRuleName());
        assertNull(rule.getRuleType());
        assertNull(rule.getConditionCategory());
        assertNull(rule.getConditionAmountMin());
        assertNull(rule.getConditionAmountMax());
        assertNull(rule.getConditionAgeDays());
        assertNull(rule.getConditionReceiptRequired());
        assertNull(rule.getActionApprovalStatus());
        assertNull(rule.getActionReason());
        assertNull(rule.getCreatedAt());
        assertNull(rule.getUpdatedAt());

        // Test setters
        rule.setId(10L);
        assertEquals(10L, rule.getId());

        rule.setRuleName("Test Rule");
        assertEquals("Test Rule", rule.getRuleName());

        rule.setRuleType(PolicyRuleType.AUTO_APPROVE);
        assertEquals(PolicyRuleType.AUTO_APPROVE, rule.getRuleType());

        rule.setEnabled(false);
        assertFalse(rule.getEnabled());

        rule.setPriority(5);
        assertEquals(5, rule.getPriority());

        rule.setConditionCategory(ExpenseCategory.TRAVEL);
        assertEquals(ExpenseCategory.TRAVEL, rule.getConditionCategory());

        BigDecimal minAmount = new BigDecimal("10.00");
        rule.setConditionAmountMin(minAmount);
        assertEquals(minAmount, rule.getConditionAmountMin());

        BigDecimal maxAmount = new BigDecimal("100.00");
        rule.setConditionAmountMax(maxAmount);
        assertEquals(maxAmount, rule.getConditionAmountMax());

        rule.setConditionAgeDays(30);
        assertEquals(30, rule.getConditionAgeDays());

        rule.setConditionReceiptRequired(true);
        assertTrue(rule.getConditionReceiptRequired());

        rule.setActionApprovalStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, rule.getActionApprovalStatus());

        rule.setActionReason("Auto approved within limit");
        assertEquals("Auto approved within limit", rule.getActionReason());
    }

    @Test
    void testLifecycleHooks() {
        PolicyRule rule = new PolicyRule();

        assertNull(rule.getCreatedAt());
        assertNull(rule.getUpdatedAt());

        rule.onCreate();

        assertNotNull(rule.getCreatedAt());
        assertNotNull(rule.getUpdatedAt());

        rule.onUpdate();

        assertNotNull(rule.getUpdatedAt());
    }
}