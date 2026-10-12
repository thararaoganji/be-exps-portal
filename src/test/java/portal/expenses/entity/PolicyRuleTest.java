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

        // Set properties
        rule.setId(10L);
        rule.setRuleName("High Meal Expense");
        rule.setEnabled(false);
        rule.setPriority(5);
        rule.setConditionAmountMin(new BigDecimal("100.00"));
        rule.setConditionAmountMax(new BigDecimal("500.00"));
        rule.setConditionAgeDays(30);
        rule.setConditionReceiptRequired(true);
        rule.setActionReason("Meal exceeds policy threshold");

        // Pass null or existing enum instances if available via values()
        PolicyRuleType[] ruleTypes = PolicyRuleType.values();
        if (ruleTypes.length > 0) {
            rule.setRuleType(ruleTypes[0]);
            assertEquals(ruleTypes[0], rule.getRuleType());
        }

        ExpenseCategory[] categories = ExpenseCategory.values();
        if (categories.length > 0) {
            rule.setConditionCategory(categories[0]);
            assertEquals(categories[0], rule.getConditionCategory());
        }

        ApprovalStatus[] statuses = ApprovalStatus.values();
        if (statuses.length > 0) {
            rule.setActionApprovalStatus(statuses[0]);
            assertEquals(statuses[0], rule.getActionApprovalStatus());
        }

        // Assert properties
        assertEquals(10L, rule.getId());
        assertEquals("High Meal Expense", rule.getRuleName());
        assertFalse(rule.getEnabled());
        assertEquals(5, rule.getPriority());
        assertEquals(new BigDecimal("100.00"), rule.getConditionAmountMin());
        assertEquals(new BigDecimal("500.00"), rule.getConditionAmountMax());
        assertEquals(30, rule.getConditionAgeDays());
        assertTrue(rule.getConditionReceiptRequired());
        assertEquals("Meal exceeds policy threshold", rule.getActionReason());
    }

    @Test
    void testLifecycleCallbacks() {
        PolicyRule rule = new PolicyRule();

        assertNull(rule.getCreatedAt());
        assertNull(rule.getUpdatedAt());

        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);
        rule.onCreate();
        LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);

        assertNotNull(rule.getCreatedAt());
        assertNotNull(rule.getUpdatedAt());
        assertTrue(rule.getCreatedAt().isAfter(beforeCreate) && rule.getCreatedAt().isBefore(afterCreate));
        assertTrue(rule.getUpdatedAt().isAfter(beforeCreate) && rule.getUpdatedAt().isBefore(afterCreate));

        LocalDateTime originalCreatedAt = rule.getCreatedAt();

        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        rule.onUpdate();
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);

        assertEquals(originalCreatedAt, rule.getCreatedAt());
        assertNotNull(rule.getUpdatedAt());
        assertTrue(rule.getUpdatedAt().isAfter(beforeUpdate) && rule.getUpdatedAt().isBefore(afterUpdate));
    }
}