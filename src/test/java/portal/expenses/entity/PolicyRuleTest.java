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

        // Set and get id
        rule.setId(100L);
        assertEquals(100L, rule.getId());

        // Set and get ruleName
        rule.setRuleName("Test Rule");
        assertEquals("Test Rule", rule.getRuleName());

        // Set and get ruleType
        rule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        assertEquals(PolicyRuleType.AMOUNT_THRESHOLD, rule.getRuleType());

        // Set and get enabled
        rule.setEnabled(false);
        assertFalse(rule.getEnabled());

        // Set and get priority
        rule.setPriority(5);
        assertEquals(5, rule.getPriority());

        // Set and get conditionCategory
        rule.setConditionCategory(ExpenseCategory.MEALS);
        assertEquals(ExpenseCategory.MEALS, rule.getConditionCategory());

        // Set and get conditionAmountMin
        BigDecimal minAmount = new BigDecimal("10.00");
        rule.setConditionAmountMin(minAmount);
        assertEquals(minAmount, rule.getConditionAmountMin());

        // Set and get conditionAmountMax
        BigDecimal maxAmount = new BigDecimal("100.00");
        rule.setConditionAmountMax(maxAmount);
        assertEquals(maxAmount, rule.getConditionAmountMax());

        // Set and get conditionAgeDays
        rule.setConditionAgeDays(30);
        assertEquals(30, rule.getConditionAgeDays());

        // Set and get conditionReceiptRequired
        rule.setConditionReceiptRequired(true);
        assertTrue(rule.getConditionReceiptRequired());

        // Set and get actionApprovalStatus
        rule.setActionApprovalStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, rule.getActionApprovalStatus());

        // Set and get actionReason
        rule.setActionReason("Auto approved within threshold");
        assertEquals("Auto approved within threshold", rule.getActionReason());
    }

    @Test
    void testLifecycleCallbacks() throws InterruptedException {
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
        LocalDateTime previousUpdatedAt = rule.getUpdatedAt();

        Thread.sleep(10);

        rule.onUpdate();

        assertEquals(originalCreatedAt, rule.getCreatedAt());
        assertNotNull(rule.getUpdatedAt());
        assertTrue(rule.getUpdatedAt().isAfter(previousUpdatedAt) || rule.getUpdatedAt().isEqual(previousUpdatedAt));
    }
}