package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PolicyRuleTest {

    @Test
    void testGettersAndSetters() {
        PolicyRule rule = new PolicyRule();

        // Check defaults
        assertTrue(rule.getEnabled());
        assertEquals(0, rule.getPriority());

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

        rule.setActionReason("Auto-approved by rule");
        assertEquals("Auto-approved by rule", rule.getActionReason());
    }

    @Test
    void testLifecycleCallbacks() {
        PolicyRule rule = new PolicyRule();
        assertNull(rule.getCreatedAt());
        assertNull(rule.getUpdatedAt());

        rule.onCreate();
        LocalDateTime createdAt = rule.getCreatedAt();
        LocalDateTime updatedAt = rule.getUpdatedAt();

        assertNotNull(createdAt);
        assertNotNull(updatedAt);

        rule.onUpdate();
        assertNotNull(rule.getUpdatedAt());
    }
}