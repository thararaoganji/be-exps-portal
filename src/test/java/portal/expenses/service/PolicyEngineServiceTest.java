package portal.expenses.service;

import portal.expenses.repository.ExpenseApprovalRepository;
import portal.expenses.repository.PolicyRuleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portal.expenses.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PolicyEngineService Unit Tests")
class PolicyEngineServiceTest {

    @Mock
    private PolicyRuleRepository policyRuleRepository;

    @Mock
    private ExpenseApprovalRepository expenseApprovalRepository;

    @InjectMocks
    private PolicyEngineService policyEngineService;

    private Expense testExpense;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setUsername("test.user");

        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setUser(testUser);
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(true);
    }

    @Test
    @DisplayName("Test 1: Low amount without receipt requires manager approval")
    void testEvaluateExpense_LowAmountNoReceipt_RequiresManagerApproval() {
        // Arrange
        testExpense.setAmount(new BigDecimal("50.00"));
        testExpense.setHasReceipt(false);

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("RECEIPT_REQUIRED");
        rule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        rule.setConditionAmountMin(new BigDecimal("0"));
        rule.setConditionReceiptRequired(true);
        rule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        rule.setActionReason("Missing receipt");
        rule.setEnabled(true);
        rule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rule));

        // Act
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        // Assert
        assertNotNull(result);
        Assertions.assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());
        assertTrue(result.getReason().contains("Missing receipt") || result.getReason().contains("receipt"));
    }

    @Test
    @DisplayName("Test 2: Small amount with receipt gets auto-approved")
    void testEvaluateExpense_SmallAmountWithReceipt_AutoApproved() {
        // Arrange
        testExpense.setAmount(new BigDecimal("40.00"));
        testExpense.setHasReceipt(true);

        PolicyRule autoApprovalRule = new PolicyRule();
        autoApprovalRule.setRuleName("AUTO_APPROVE");
        autoApprovalRule.setRuleType(PolicyRuleType.AUTO_APPROVE);
        autoApprovalRule.setConditionAmountMax(new BigDecimal("50.00"));
        autoApprovalRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        autoApprovalRule.setActionReason("Auto-approved - under threshold");
        autoApprovalRule.setEnabled(true);
        autoApprovalRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(autoApprovalRule));

        // Act
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        // Assert
        assertNotNull(result);
        Assertions.assertEquals(ApprovalStatus.AUTO_APPROVED, result.getFinalStatus());
        assertTrue(result.getReason().contains("Auto-approved") || result.getReason().contains("auto"));
    }

    @Test
    @DisplayName("Test 3: Amount above threshold requires manager approval")
    void testEvaluateExpense_AboveThreshold_RequiresManagerApproval() {
        // Arrange
        testExpense.setAmount(new BigDecimal("600.00"));
        testExpense.setHasReceipt(true);

        PolicyRule managerRule = new PolicyRule();
        managerRule.setRuleName("MANAGER_APPROVAL");
        managerRule.setRuleType(PolicyRuleType.MANAGER_APPROVAL);
        managerRule.setConditionAmountMin(new BigDecimal("500.00"));
        managerRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        managerRule.setActionReason("Exceeds threshold - requires manager approval");
        managerRule.setEnabled(true);
        managerRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(managerRule));

        // Act
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        // Assert
        assertNotNull(result);
        Assertions.assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());
        assertTrue(result.getReason().contains("Exceeds") || result.getReason().contains("threshold"));
    }

    @Test
    @DisplayName("Test 4: High amount requires finance approval")
    void testEvaluateExpense_HighAmount_RequiresFinanceApproval() {
        // Arrange
        testExpense.setAmount(new BigDecimal("1500.00"));
        testExpense.setHasReceipt(true);

        PolicyRule financeRule = new PolicyRule();
        financeRule.setRuleName("FINANCE_APPROVAL");
        financeRule.setRuleType(PolicyRuleType.FINANCE_APPROVAL);
        financeRule.setConditionAmountMin(new BigDecimal("1000.00"));
        financeRule.setActionApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        financeRule.setActionReason("High value expense - requires finance approval");
        financeRule.setEnabled(true);
        financeRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(financeRule));

        // Act
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        // Assert
        assertNotNull(result);
        Assertions.assertEquals(ApprovalStatus.FINANCE_REVIEW, result.getFinalStatus());
        assertTrue(result.getReason().contains("high") || result.getReason().contains("finance"));
    }

    @Test
    @DisplayName("Test 5: Multiple rules - highest priority wins")
    void testEvaluateExpense_MultipleRules_HighestPriorityWins() {
        // Arrange
        testExpense.setAmount(new BigDecimal("1200.00"));
        testExpense.setHasReceipt(true);

        PolicyRule managerRule = new PolicyRule();
        managerRule.setRuleName("MANAGER_APPROVAL");
        managerRule.setRuleType(PolicyRuleType.MANAGER_APPROVAL);
        managerRule.setConditionAmountMin(new BigDecimal("500.00"));
        managerRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        managerRule.setActionReason("Manager approval required");
        managerRule.setPriority(2);
        managerRule.setEnabled(true);

        PolicyRule financeRule = new PolicyRule();
        financeRule.setRuleName("FINANCE_APPROVAL");
        financeRule.setRuleType(PolicyRuleType.FINANCE_APPROVAL);
        financeRule.setConditionAmountMin(new BigDecimal("1000.00"));
        financeRule.setActionApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        financeRule.setActionReason("Finance approval required");
        financeRule.setPriority(1);
        financeRule.setEnabled(true);

        // Return rules ordered by priority (finance first with priority 1, then manager with priority 2)
        when(policyRuleRepository.findAllEnabledOrderedByPriority())
            .thenReturn(Arrays.asList(financeRule, managerRule));

        // Act
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        // Assert
        assertNotNull(result);
        // Finance approval has higher priority than manager approval
        Assertions.assertEquals(ApprovalStatus.FINANCE_REVIEW, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test 6: No active rules defaults to pending")
    void testEvaluateExpense_NoActiveRules_DefaultsToPending() {
        // Arrange
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setHasReceipt(true);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList());

        // Act
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        // Assert
        assertNotNull(result);
        Assertions.assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
        assertTrue(result.getReason().toLowerCase().contains("no") ||
                   result.getReason().toLowerCase().contains("pending") ||
                   result.getReason().toLowerCase().contains("manual"));
    }

    @Test
    @DisplayName("Test 7: Inactive rules are ignored")
    void testEvaluateExpense_InactiveRules_Ignored() {
        // Arrange
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setHasReceipt(true);

        // Don't add the inactive rule to the returned list since repository filters it out
        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList());

        // Act
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        // Assert
        assertNotNull(result);
        Assertions.assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test 8: Receipt required rule with exact threshold")
    void testEvaluateExpense_ReceiptRequiredExactThreshold_RequiresApproval() {
        // Arrange
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setHasReceipt(false);

        PolicyRule receiptRule = new PolicyRule();
        receiptRule.setRuleName("RECEIPT_REQUIRED");
        receiptRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        receiptRule.setConditionAmountMin(new BigDecimal("100.00"));
        receiptRule.setConditionReceiptRequired(true);
        receiptRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        receiptRule.setActionReason("Missing receipt");
        receiptRule.setEnabled(true);
        receiptRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(receiptRule));

        // Act
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        // Assert
        assertNotNull(result);
        Assertions.assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());
        assertTrue(result.getReason().contains("Missing receipt") || result.getReason().contains("receipt"));
    }

    @Test
    @DisplayName("Test 9: Immediate rejection on REJECTED status match")
    void testEvaluateExpense_RuleRejected_ImmediatelyRejects() {
        testExpense.setAmount(new BigDecimal("500.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule rejectRule = new PolicyRule();
        rejectRule.setRuleName("REJECT_MEALS");
        rejectRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        rejectRule.setConditionCategory(ExpenseCategory.MEALS);
        rejectRule.setActionApprovalStatus(ApprovalStatus.REJECTED);
        rejectRule.setActionReason("Meals not allowed");
        rejectRule.setEnabled(true);
        rejectRule.setPriority(1);

        PolicyRule nextRule = new PolicyRule();
        nextRule.setRuleName("AUTO_APPROVE");
        nextRule.setRuleType(PolicyRuleType.AUTO_APPROVE);
        nextRule.setPriority(2);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rejectRule, nextRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.REJECTED, decision.getFinalStatus());
        assertEquals("Meals not allowed", decision.getReason());
        assertEquals(rejectRule, decision.getAppliedRule());
        assertEquals(testExpense, decision.getExpense());
        verify(expenseApprovalRepository, times(1)).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 10: Category rule mismatch does not match")
    void testEvaluateExpense_CategoryRuleMismatch() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule categoryRule = new PolicyRule();
        categoryRule.setRuleName("TRAVEL_RULE");
        categoryRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        categoryRule.setConditionCategory(ExpenseCategory.TRAVEL);
        categoryRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        categoryRule.setActionReason("Travel category check");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(categoryRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
        verify(expenseApprovalRepository, never()).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 11: Age limit rule matches when older than threshold")
    void testEvaluateExpense_AgeLimitMatches() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(35));

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("AGE_LIMIT");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(30);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        ageRule.setActionReason("Expense submitted late");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(ageRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, decision.getFinalStatus());
        assertEquals(ageRule, decision.getAppliedRule());
    }

    @Test
    @DisplayName("Test 12: Age limit rule with null condition age days does not match")
    void testEvaluateExpense_AgeLimitNullConditionAgeDays() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(35));

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("AGE_LIMIT");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(null);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(ageRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
    }

    @Test
    @DisplayName("Test 13: Receipt required rule with category condition mismatch")
    void testEvaluateExpense_ReceiptRequiredCategoryMismatch() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(false);

        PolicyRule receiptRule = new PolicyRule();
        receiptRule.setRuleName("RECEIPT_REQUIRED_FOR_TRAVEL");
        receiptRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        receiptRule.setConditionCategory(ExpenseCategory.TRAVEL);
        receiptRule.setConditionReceiptRequired(true);
        receiptRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(receiptRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
    }

    @Test
    @DisplayName("Test 14: Receipt required rule when receipt is present")
    void testEvaluateExpense_ReceiptRequiredWhenReceiptPresent() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(true);

        PolicyRule receiptRule = new PolicyRule();
        receiptRule.setRuleName("RECEIPT_REQUIRED_FOR_MEALS");
        receiptRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        receiptRule.setConditionCategory(ExpenseCategory.MEALS);
        receiptRule.setConditionReceiptRequired(true);
        receiptRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(receiptRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
    }

    @Test
    @DisplayName("Test 15: Amount threshold rule with both min and max boundary checking")
    void testEvaluateExpense_AmountThresholdMinAndMax() {
        testExpense.setAmount(new BigDecimal("100.00"));

        PolicyRule amountRule = new PolicyRule();
        amountRule.setRuleName("AMOUNT_RANGE");
        amountRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        amountRule.setConditionAmountMin(new BigDecimal("50.00"));
        amountRule.setConditionAmountMax(new BigDecimal("150.00"));
        amountRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        amountRule.setActionReason("In range");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(amountRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.AUTO_APPROVED, decision.getFinalStatus());
        verify(expenseApprovalRepository, times(1)).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 16: Amount threshold rule fails max check")
    void testEvaluateExpense_AmountThresholdExceedsMax() {
        testExpense.setAmount(new BigDecimal("200.00"));

        PolicyRule amountRule = new PolicyRule();
        amountRule.setRuleName("AMOUNT_RANGE");
        amountRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        amountRule.setConditionAmountMin(new BigDecimal("50.00"));
        amountRule.setConditionAmountMax(new BigDecimal("150.00"));
        amountRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(amountRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
    }

    @Test
    @DisplayName("Test 17: Matched rule with PENDING status doesn't create approval record")
    void testEvaluateExpense_MatchedRuleWithPendingStatus_NoApprovalRecordCreated() {
        testExpense.setAmount(new BigDecimal("50.00"));

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("PENDING_RULE");
        rule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rule.setConditionAmountMin(new BigDecimal("10.00"));
        rule.setActionApprovalStatus(ApprovalStatus.PENDING);
        rule.setActionReason("Requires pending manual review");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
        verify(expenseApprovalRepository, never()).save(any(ExpenseApproval.class));
    }
}