package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PolicyRuleTest {

    @Test
    void testGettersAndSetters() {
        PolicyRule rule = new PolicyRule();

        rule.setId(100L);
        assertEquals(100L, rule.getId());

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

        rule.setActionReason("Auto-approved due to policy");
        assertEquals("Auto-approved due to policy", rule.getActionReason());
    }

    @Test
    void testDefaultValues() {
        PolicyRule rule = new PolicyRule();
        assertTrue(rule.getEnabled());
        assertEquals(0, rule.getPriority());
        assertNull(rule.getCreatedAt());
        assertNull(rule.getUpdatedAt());
    }

    @Test
    void testLifecycleCallbacks() {
        PolicyRule rule = new PolicyRule();

        assertNull(rule.getCreatedAt());
        assertNull(rule.getUpdatedAt());

        rule.onCreate();
        LocalDateTime created = rule.getCreatedAt();
        LocalDateTime updated = rule.getUpdatedAt();

        assertNotNull(created);
        assertNotNull(updated);

        rule.onUpdate();
        assertNotNull(rule.getUpdatedAt());
        assertEquals(created, rule.getCreatedAt());
    }
}