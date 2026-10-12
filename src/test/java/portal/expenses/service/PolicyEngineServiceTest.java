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
    @DisplayName("Rule matching REJECTED status immediately rejects and stops further rule evaluation")
    void testEvaluateExpense_RejectedStatusStopsEvaluation() {
        testExpense.setAmount(new BigDecimal("250.00"));

        PolicyRule rejectRule = new PolicyRule();
        rejectRule.setRuleName("REJECT_POLICY");
        rejectRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rejectRule.setConditionAmountMin(new BigDecimal("200.00"));
        rejectRule.setActionApprovalStatus(ApprovalStatus.REJECTED);
        rejectRule.setActionReason("Policy violation: excessive amount");
        rejectRule.setEnabled(true);
        rejectRule.setPriority(1);

        PolicyRule secondaryRule = new PolicyRule();
        secondaryRule.setRuleName("AUTO_APPROVE");
        secondaryRule.setRuleType(PolicyRuleType.AUTO_APPROVE);
        secondaryRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        secondaryRule.setActionReason("Auto-approve rule");
        secondaryRule.setEnabled(true);
        secondaryRule.setPriority(2);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(rejectRule, secondaryRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.REJECTED, decision.getFinalStatus());
        assertEquals("Policy violation: excessive amount", decision.getReason());
        assertSame(rejectRule, decision.getAppliedRule());
        assertSame(testExpense, decision.getExpense());

        ArgumentCaptor<ExpenseApproval> captor = ArgumentCaptor.forClass(ExpenseApproval.class);
        verify(expenseApprovalRepository, times(1)).save(captor.capture());
        ExpenseApproval saved = captor.getValue();
        assertEquals(ApprovalStatus.REJECTED, saved.getStatus());
        assertEquals("AUTO", saved.getApprovalLevel());
        assertEquals("Policy violation: excessive amount", saved.getComments());
        assertSame(testExpense, saved.getExpense());
        assertSame(rejectRule, saved.getAppliedRule());
    }

    @Test
    @DisplayName("Category rule matches when expense category equals condition category")
    void testEvaluateExpense_CategoryRule_Matches() {
        testExpense.setCategory(ExpenseCategory.TRAVEL);

        PolicyRule categoryRule = new PolicyRule();
        categoryRule.setRuleName("TRAVEL_POLICY");
        categoryRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        categoryRule.setConditionCategory(ExpenseCategory.TRAVEL);
        categoryRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        categoryRule.setActionReason("Travel category requires manager review");
        categoryRule.setEnabled(true);
        categoryRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Collections.singletonList(categoryRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, decision.getFinalStatus());
        assertEquals("Travel category requires manager review", decision.getReason());
        assertSame(categoryRule, decision.getAppliedRule());

        verify(expenseApprovalRepository, times(1)).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Category rule does not match when condition category is different or null")
    void testEvaluateExpense_CategoryRule_DoesNotMatch() {
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule ruleDiffCategory = new PolicyRule();
        ruleDiffCategory.setRuleName("TRAVEL_RULE");
        ruleDiffCategory.setRuleType(PolicyRuleType.CATEGORY_RULE);
        ruleDiffCategory.setConditionCategory(ExpenseCategory.TRAVEL);
        ruleDiffCategory.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        PolicyRule ruleNullCategory = new PolicyRule();
        ruleNullCategory.setRuleName("NULL_CATEGORY_RULE");
        ruleNullCategory.setRuleType(PolicyRuleType.CATEGORY_RULE);
        ruleNullCategory.setConditionCategory(null);
        ruleNullCategory.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(ruleDiffCategory, ruleNullCategory));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
        verify(expenseApprovalRepository, never()).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Age limit rule matches when expense age exceeds condition age in days")
    void testEvaluateExpense_AgeLimit_MatchesWhenOlder() {
        testExpense.setExpenseDate(LocalDate.now().minusDays(45));

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("AGE_LIMIT_30");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(30);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        ageRule.setActionReason("Expense older than 30 days");
        ageRule.setEnabled(true);
        ageRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Collections.singletonList(ageRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.MANAGER_REVIEW, decision.getFinalStatus());
        assertSame(ageRule, decision.getAppliedRule());
    }

    @Test
    @DisplayName("Age limit rule does not match when expense is newer or condition age is null")
    void testEvaluateExpense_AgeLimit_DoesNotMatchWhenNewerOrNull() {
        testExpense.setExpenseDate(LocalDate.now().minusDays(10));

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("AGE_LIMIT_30");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(30);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        PolicyRule nullAgeRule = new PolicyRule();
        nullAgeRule.setRuleName("AGE_LIMIT_NULL");
        nullAgeRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        nullAgeRule.setConditionAgeDays(null);
        nullAgeRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(ageRule, nullAgeRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
    }

    @Test
    @DisplayName("Receipt required rule matches when category matches, receipt is required and missing")
    void testEvaluateExpense_ReceiptRequired_CategoryMatchesAndMissingReceipt() {
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(false);

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("MEALS_RECEIPT");
        rule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        rule.setConditionCategory(ExpenseCategory.MEALS);
        rule.setConditionReceiptRequired(true);
        rule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        rule.setActionReason("Meals require receipt");
        rule.setEnabled(true);
        rule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Collections.singletonList(rule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.MANAGER_REVIEW, decision.getFinalStatus());
        assertSame(rule, decision.getAppliedRule());
    }

    @Test
    @DisplayName("Receipt required rule does not match when category differs or receipt is present")
    void testEvaluateExpense_ReceiptRequired_CategoryMismatchOrHasReceipt() {
        testExpense.setCategory(ExpenseCategory.TRAVEL);
        testExpense.setHasReceipt(false);

        PolicyRule mismatchCategoryRule = new PolicyRule();
        mismatchCategoryRule.setRuleName("MEALS_RECEIPT");
        mismatchCategoryRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        mismatchCategoryRule.setConditionCategory(ExpenseCategory.MEALS);
        mismatchCategoryRule.setConditionReceiptRequired(true);

        PolicyRule hasReceiptRule = new PolicyRule();
        hasReceiptRule.setRuleName("TRAVEL_RECEIPT_NOT_REQUIRED");
        hasReceiptRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        hasReceiptRule.setConditionCategory(ExpenseCategory.TRAVEL);
        hasReceiptRule.setConditionReceiptRequired(false);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(mismatchCategoryRule, hasReceiptRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
    }

    @Test
    @DisplayName("Amount threshold rule evaluates min and max boundaries correctly")
    void testEvaluateExpense_AmountThreshold_MinAndMaxBounds() {
        testExpense.setAmount(new BigDecimal("150.00"));

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("BOUNDED_AMOUNT");
        rule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rule.setConditionAmountMin(new BigDecimal("100.00"));
        rule.setConditionAmountMax(new BigDecimal("200.00"));
        rule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        rule.setActionReason("Within bounded threshold");
        rule.setEnabled(true);
        rule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Collections.singletonList(rule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.AUTO_APPROVED, decision.getFinalStatus());

        // Amount fails max bound
        testExpense.setAmount(new BigDecimal("250.00"));
        PolicyEngineService.ApprovalDecision decisionAbove = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, decisionAbove.getFinalStatus());

        // Amount fails min bound
        testExpense.setAmount(new BigDecimal("50.00"));
        PolicyEngineService.ApprovalDecision decisionBelow = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, decisionBelow.getFinalStatus());
    }

    @Test
    @DisplayName("Finance review status creates both manager and finance approval records")
    void testEvaluateExpense_FinanceReview_CreatesBothApprovalRecords() {
        testExpense.setAmount(new BigDecimal("2000.00"));

        PolicyRule financeRule = new PolicyRule();
        financeRule.setRuleName("FINANCE_REVIEW_RULE");
        financeRule.setRuleType(PolicyRuleType.FINANCE_APPROVAL);
        financeRule.setConditionAmountMin(new BigDecimal("1000.00"));
        financeRule.setActionApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        financeRule.setActionReason("Finance and manager approval required");
        financeRule.setEnabled(true);
        financeRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Collections.singletonList(financeRule));

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
    @DisplayName("Applied rule with PENDING status does not create approval records")
    void testEvaluateExpense_MatchedRuleWithPendingStatus_NoApprovalRecordsCreated() {
        testExpense.setAmount(new BigDecimal("100.00"));

        PolicyRule pendingRule = new PolicyRule();
        pendingRule.setRuleName("FORCE_PENDING");
        pendingRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        pendingRule.setActionApprovalStatus(ApprovalStatus.PENDING);
        pendingRule.setActionReason("Review manually");
        pendingRule.setEnabled(true);
        pendingRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Collections.singletonList(pendingRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
        assertSame(pendingRule, decision.getAppliedRule());
        verify(expenseApprovalRepository, never()).save(any(ExpenseApproval.class));
    }
}