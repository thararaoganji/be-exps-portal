package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PolicyRuleTest {

    @Test
    void testGettersAndSetters() {
        PolicyRule rule = new PolicyRule();

        rule.setId(10L);
        rule.setRuleName("Rule 1");
        rule.setRuleType(null);
        rule.setEnabled(false);
        rule.setPriority(5);
        rule.setConditionCategory(null);
        rule.setConditionAmountMin(new BigDecimal("10.00"));
        rule.setConditionAmountMax(new BigDecimal("100.00"));
        rule.setConditionAgeDays(30);
        rule.setConditionReceiptRequired(true);
        rule.setActionApprovalStatus(null);
        rule.setActionReason("Auto-approved");

        assertEquals(10L, rule.getId());
        assertEquals("Rule 1", rule.getRuleName());
        assertNull(rule.getRuleType());
        assertFalse(rule.getEnabled());
        assertEquals(5, rule.getPriority());
        assertNull(rule.getConditionCategory());
        assertEquals(new BigDecimal("10.00"), rule.getConditionAmountMin());
        assertEquals(new BigDecimal("100.00"), rule.getConditionAmountMax());
        assertEquals(30, rule.getConditionAgeDays());
        assertTrue(rule.getConditionReceiptRequired());
        assertNull(rule.getActionApprovalStatus());
        assertEquals("Auto-approved", rule.getActionReason());
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
    void testLifecycleHooks() {
        PolicyRule rule = new PolicyRule();

        assertNull(rule.getCreatedAt());
        assertNull(rule.getUpdatedAt());

        rule.onCreate();

        assertNotNull(rule.getCreatedAt());
        assertNotNull(rule.getUpdatedAt());
        LocalDateTime initialCreatedAt = rule.getCreatedAt();
        LocalDateTime initialUpdatedAt = rule.getUpdatedAt();

        rule.onUpdate();

        assertEquals(initialCreatedAt, rule.getCreatedAt());
        assertNotNull(rule.getUpdatedAt());
        assertTrue(!rule.getUpdatedAt().isBefore(initialUpdatedAt));
    }
}