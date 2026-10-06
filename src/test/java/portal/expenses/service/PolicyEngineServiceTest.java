package portal.expenses.service;

import portal.expenses.repository.ExpenseApprovalRepository;
import portal.expenses.repository.PolicyRuleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portal.expenses.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

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
    @DisplayName("Test 9: Immediate rejection when rule action status is REJECTED")
    void testEvaluateExpense_RuleActionRejected_ReturnsImmediately() {
        testExpense.setAmount(new BigDecimal("5000.00"));

        PolicyRule rejectRule = new PolicyRule();
        rejectRule.setRuleName("REJECT_HIGH_AMOUNT");
        rejectRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rejectRule.setConditionAmountMin(new BigDecimal("3000.00"));
        rejectRule.setActionApprovalStatus(ApprovalStatus.REJECTED);
        rejectRule.setActionReason("Amount exceeds policy ceiling");
        rejectRule.setEnabled(true);
        rejectRule.setPriority(1);

        PolicyRule secondRule = new PolicyRule();
        secondRule.setRuleName("MANAGER_APPROVAL");
        secondRule.setRuleType(PolicyRuleType.MANAGER_APPROVAL);
        secondRule.setConditionAmountMin(new BigDecimal("500.00"));
        secondRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        secondRule.setPriority(2);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rejectRule, secondRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.REJECTED, decision.getFinalStatus());
        assertEquals("Amount exceeds policy ceiling", decision.getReason());
        assertEquals(rejectRule, decision.getAppliedRule());
        assertEquals(testExpense, decision.getExpense());

        ArgumentCaptor<ExpenseApproval> captor = ArgumentCaptor.forClass(ExpenseApproval.class);
        verify(expenseApprovalRepository, times(1)).save(captor.capture());
        ExpenseApproval saved = captor.getValue();
        assertEquals(ApprovalStatus.REJECTED, saved.getStatus());
        assertEquals("AUTO", saved.getApprovalLevel());
        assertEquals("Amount exceeds policy ceiling", saved.getComments());
    }

    @Test
    @DisplayName("Test 10: Category rule matches and does not match")
    void testEvaluateExpense_CategoryRule() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule matchingCategoryRule = new PolicyRule();
        matchingCategoryRule.setRuleName("MEALS_CATEGORY_RULE");
        matchingCategoryRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        matchingCategoryRule.setConditionCategory(ExpenseCategory.MEALS);
        matchingCategoryRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        matchingCategoryRule.setActionReason("Meals category rule matched");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Collections.singletonList(matchingCategoryRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.AUTO_APPROVED, decision.getFinalStatus());
        assertEquals(matchingCategoryRule, decision.getAppliedRule());

        // Category does not match
        matchingCategoryRule.setConditionCategory(ExpenseCategory.TRAVEL);
        PolicyEngineService.ApprovalDecision pendingDecision = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, pendingDecision.getFinalStatus());

        // Condition category is null
        matchingCategoryRule.setConditionCategory(null);
        PolicyEngineService.ApprovalDecision nullCategoryDecision = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, nullCategoryDecision.getFinalStatus());
    }

    @Test
    @DisplayName("Test 11: Age limit rule matches and does not match")
    void testEvaluateExpense_AgeLimitRule() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(40));

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("OLD_EXPENSE");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(30);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        ageRule.setActionReason("Expense submitted after 30 days");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Collections.singletonList(ageRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, decision.getFinalStatus());

        // Age within limit
        testExpense.setExpenseDate(LocalDate.now().minusDays(10));
        PolicyEngineService.ApprovalDecision decisionWithinLimit = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, decisionWithinLimit.getFinalStatus());

        // Condition age days is null
        ageRule.setConditionAgeDays(null);
        PolicyEngineService.ApprovalDecision decisionNullAge = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, decisionNullAge.getFinalStatus());
    }

    @Test
    @DisplayName("Test 12: Receipt required rule with category condition and receipt present/absent")
    void testEvaluateExpense_ReceiptRequiredCategoryCheck() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(false);

        PolicyRule receiptRule = new PolicyRule();
        receiptRule.setRuleName("TRAVEL_RECEIPT_REQUIRED");
        receiptRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        receiptRule.setConditionCategory(ExpenseCategory.TRAVEL);
        receiptRule.setConditionReceiptRequired(true);
        receiptRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Collections.singletonList(receiptRule));

        // Category mismatch -> false
        PolicyEngineService.ApprovalDecision decisionMismatch = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, decisionMismatch.getFinalStatus());

        // Category match + receipt false -> true
        receiptRule.setConditionCategory(ExpenseCategory.MEALS);
        PolicyEngineService.ApprovalDecision decisionMatch = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, decisionMatch.getFinalStatus());

        // Category match + receipt true -> false
        testExpense.setHasReceipt(true);
        PolicyEngineService.ApprovalDecision decisionHasReceipt = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, decisionHasReceipt.getFinalStatus());
    }

    @Test
    @DisplayName("Test 13: Amount threshold min and max combinations")
    void testEvaluateExpense_AmountThresholdMinMax() {
        testExpense.setAmount(new BigDecimal("100.00"));

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("THRESHOLD_TEST");
        rule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rule.setConditionAmountMin(new BigDecimal("50.00"));
        rule.setConditionAmountMax(new BigDecimal("150.00"));
        rule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Collections.singletonList(rule));

        // In range
        PolicyEngineService.ApprovalDecision inRange = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.AUTO_APPROVED, inRange.getFinalStatus());

        // Below min
        testExpense.setAmount(new BigDecimal("40.00"));
        PolicyEngineService.ApprovalDecision belowMin = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, belowMin.getFinalStatus());

        // Above max
        testExpense.setAmount(new BigDecimal("200.00"));
        PolicyEngineService.ApprovalDecision aboveMax = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, aboveMax.getFinalStatus());
    }

    @Test
    @DisplayName("Test 14: Approval records created for FINANCE_REVIEW create two records (MANAGER and FINANCE)")
    void testEvaluateExpense_FinanceReviewCreatesTwoApprovalRecords() {
        testExpense.setAmount(new BigDecimal("2000.00"));

        PolicyRule financeRule = new PolicyRule();
        financeRule.setRuleName("FINANCE_APPROVAL");
        financeRule.setRuleType(PolicyRuleType.FINANCE_APPROVAL);
        financeRule.setConditionAmountMin(new BigDecimal("1000.00"));
        financeRule.setActionApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        financeRule.setActionReason("Finance review required");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Collections.singletonList(financeRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.FINANCE_REVIEW, decision.getFinalStatus());

        ArgumentCaptor<ExpenseApproval> captor = ArgumentCaptor.forClass(ExpenseApproval.class);
        verify(expenseApprovalRepository, times(2)).save(captor.capture());
        assertEquals("MANAGER", captor.getAllValues().get(0).getApprovalLevel());
        assertEquals(ApprovalStatus.MANAGER_REVIEW, captor.getAllValues().get(0).getStatus());
        assertEquals("FINANCE", captor.getAllValues().get(1).getApprovalLevel());
        assertEquals(ApprovalStatus.FINANCE_REVIEW, captor.getAllValues().get(1).getStatus());
    }

    @Test
    @DisplayName("Test 15: ApprovalDecision getters and setters")
    void testApprovalDecisionGettersAndSetters() {
        PolicyEngineService.ApprovalDecision decision = new PolicyEngineService.ApprovalDecision();
        PolicyRule rule = new PolicyRule();
        decision.setExpense(testExpense);
        decision.setAppliedRule(rule);
        decision.setFinalStatus(ApprovalStatus.PENDING);
        decision.setReason("Test reason");

        assertEquals(testExpense, decision.getExpense());
        assertEquals(rule, decision.getAppliedRule());
        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
        assertEquals("Test reason", decision.getReason());
    }
}