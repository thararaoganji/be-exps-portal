package portal.expenses.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PolicyRuleTest {

    @Test
    void testGettersAndSetters() {
        PolicyRule rule = new PolicyRule();

        rule.setId(1L);
        rule.setRuleName("Rule Name");
        rule.setRuleType(PolicyRuleType.AUTO_APPROVE);
        rule.setEnabled(false);
        rule.setPriority(5);
        rule.setConditionCategory(ExpenseCategory.TRAVEL);
        rule.setConditionAmountMin(new BigDecimal("10.00"));
        rule.setConditionAmountMax(new BigDecimal("100.00"));
        rule.setConditionAgeDays(30);
        rule.setConditionReceiptRequired(true);
        rule.setActionApprovalStatus(ApprovalStatus.APPROVED);
        rule.setActionReason("Auto approved");

        assertEquals(1L, rule.getId());
        assertEquals("Rule Name", rule.getRuleName());
        assertEquals(PolicyRuleType.AUTO_APPROVE, rule.getRuleType());
        assertFalse(rule.getEnabled());
        assertEquals(5, rule.getPriority());
        assertEquals(ExpenseCategory.TRAVEL, rule.getConditionCategory());
        assertEquals(new BigDecimal("10.00"), rule.getConditionAmountMin());
        assertEquals(new BigDecimal("100.00"), rule.getConditionAmountMax());
        assertEquals(30, rule.getConditionAgeDays());
        assertTrue(rule.getConditionReceiptRequired());
        assertEquals(ApprovalStatus.APPROVED, rule.getActionApprovalStatus());
        assertEquals("Auto approved", rule.getActionReason());
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

        rule.onCreate();
        assertNotNull(rule.getCreatedAt());
        assertNotNull(rule.getUpdatedAt());

        LocalDateTime created = rule.getCreatedAt();
        LocalDateTime updated = rule.getUpdatedAt();

        rule.onUpdate();
        assertEquals(created, rule.getCreatedAt());
        assertNotNull(rule.getUpdatedAt());
        assertFalse(rule.getUpdatedAt().isBefore(updated));
    }
}