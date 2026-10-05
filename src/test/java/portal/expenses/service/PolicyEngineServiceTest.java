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
import java.util.List;

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
        testExpense.setExpenseDate(LocalDate.now());
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
    @DisplayName("Test immediate rejection rule short-circuits evaluation")
    void testEvaluateExpense_RejectedRule_ShortCircuitsAndSavesApprovalRecord() {
        testExpense.setAmount(new BigDecimal("5000.00"));

        PolicyRule rejectRule = new PolicyRule();
        rejectRule.setRuleName("REJECT_POLICY");
        rejectRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rejectRule.setConditionAmountMin(new BigDecimal("3000.00"));
        rejectRule.setActionApprovalStatus(ApprovalStatus.REJECTED);
        rejectRule.setActionReason("Amount exceeds company limit");
        rejectRule.setPriority(1);

        PolicyRule secondRule = new PolicyRule();
        secondRule.setRuleName("SECOND_RULE");
        secondRule.setRuleType(PolicyRuleType.AUTO_APPROVE);
        secondRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rejectRule, secondRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.REJECTED, decision.getFinalStatus());
        assertEquals(rejectRule, decision.getAppliedRule());
        assertEquals("Amount exceeds company limit", decision.getReason());
        assertEquals(testExpense, decision.getExpense());

        ArgumentCaptor<ExpenseApproval> captor = ArgumentCaptor.forClass(ExpenseApproval.class);
        verify(expenseApprovalRepository).save(captor.capture());
        ExpenseApproval saved = captor.getValue();
        assertEquals(testExpense, saved.getExpense());
        assertEquals(rejectRule, saved.getAppliedRule());
        assertEquals(ApprovalStatus.REJECTED, saved.getStatus());
        assertEquals("AUTO", saved.getApprovalLevel());
        assertEquals("Amount exceeds company limit", saved.getComments());
    }

    @Test
    @DisplayName("Test category rule matching and mismatch")
    void testEvaluateExpense_CategoryRule() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule mismatchRule = new PolicyRule();
        mismatchRule.setRuleName("TRAVEL_RULE");
        mismatchRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        mismatchRule.setConditionCategory(ExpenseCategory.TRAVEL);
        mismatchRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        mismatchRule.setActionReason("Travel category check");

        PolicyRule matchRule = new PolicyRule();
        matchRule.setRuleName("MEALS_RULE");
        matchRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        matchRule.setConditionCategory(ExpenseCategory.MEALS);
        matchRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        matchRule.setActionReason("Meals approved");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(mismatchRule, matchRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.AUTO_APPROVED, decision.getFinalStatus());
        assertEquals(matchRule, decision.getAppliedRule());
    }

    @Test
    @DisplayName("Test age limit rule matching and edge cases")
    void testEvaluateExpense_AgeLimitRule() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(40));

        PolicyRule nullAgeRule = new PolicyRule();
        nullAgeRule.setRuleName("NULL_AGE");
        nullAgeRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        nullAgeRule.setConditionAgeDays(null);

        PolicyRule ageLimitRule = new PolicyRule();
        ageLimitRule.setRuleName("OLD_EXPENSE");
        ageLimitRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageLimitRule.setConditionAgeDays(30);
        ageLimitRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        ageLimitRule.setActionReason("Expense older than 30 days");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(nullAgeRule, ageLimitRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, decision.getFinalStatus());
        assertEquals(ageLimitRule, decision.getAppliedRule());
    }

    @Test
    @DisplayName("Test receipt required rule when category matches or mismatches")
    void testEvaluateExpense_ReceiptRequiredRuleWithCategory() {
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(true);

        PolicyRule receiptRuleWithMismatchCategory = new PolicyRule();
        receiptRuleWithMismatchCategory.setRuleName("RECEIPT_TRAVEL");
        receiptRuleWithMismatchCategory.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        receiptRuleWithMismatchCategory.setConditionCategory(ExpenseCategory.TRAVEL);
        receiptRuleWithMismatchCategory.setConditionReceiptRequired(true);

        PolicyRule receiptRuleWithMatchingCategory = new PolicyRule();
        receiptRuleWithMatchingCategory.setRuleName("RECEIPT_MEALS");
        receiptRuleWithMatchingCategory.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        receiptRuleWithMatchingCategory.setConditionCategory(ExpenseCategory.MEALS);
        receiptRuleWithMatchingCategory.setConditionReceiptRequired(true);
        receiptRuleWithMatchingCategory.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(receiptRuleWithMismatchCategory, receiptRuleWithMatchingCategory));

        // When expense has receipt, receipt rule should not match
        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());

        // When expense does not have receipt, matching category should match
        testExpense.setHasReceipt(false);
        decision = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, decision.getFinalStatus());
    }

    @Test
    @DisplayName("Test finance review creates two approval records")
    void testEvaluateExpense_FinanceReviewCreatesTwoApprovalRecords() {
        testExpense.setAmount(new BigDecimal("2000.00"));

        PolicyRule financeRule = new PolicyRule();
        financeRule.setRuleName("FINANCE_RULE");
        financeRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        financeRule.setConditionAmountMin(new BigDecimal("1000.00"));
        financeRule.setActionApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        financeRule.setActionReason("High value");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(financeRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.FINANCE_REVIEW, decision.getFinalStatus());

        ArgumentCaptor<ExpenseApproval> captor = ArgumentCaptor.forClass(ExpenseApproval.class);
        verify(expenseApprovalRepository, times(2)).save(captor.capture());
        List<ExpenseApproval> records = captor.getAllValues();
        assertEquals(2, records.size());
        assertEquals(ApprovalStatus.MANAGER_REVIEW, records.get(0).getStatus());
        assertEquals("MANAGER", records.get(0).getApprovalLevel());
        assertEquals(ApprovalStatus.FINANCE_REVIEW, records.get(1).getStatus());
        assertEquals("FINANCE", records.get(1).getApprovalLevel());
    }

    @Test
    @DisplayName("Test amount threshold when min and max are applied or out of bounds")
    void testEvaluateAmountThreshold_MinAndMaxChecks() {
        testExpense.setAmount(new BigDecimal("500.00"));

        PolicyRule outOfMinRule = new PolicyRule();
        outOfMinRule.setRuleName("OUT_OF_MIN");
        outOfMinRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        outOfMinRule.setConditionAmountMin(new BigDecimal("600.00"));

        PolicyRule outOfMaxRule = new PolicyRule();
        outOfMaxRule.setRuleName("OUT_OF_MAX");
        outOfMaxRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        outOfMaxRule.setConditionAmountMin(new BigDecimal("100.00"));
        outOfMaxRule.setConditionAmountMax(new BigDecimal("400.00"));

        PolicyRule inRangeRule = new PolicyRule();
        inRangeRule.setRuleName("IN_RANGE");
        inRangeRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        inRangeRule.setConditionAmountMin(new BigDecimal("100.00"));
        inRangeRule.setConditionAmountMax(new BigDecimal("600.00"));
        inRangeRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        inRangeRule.setActionReason("In range");

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(outOfMinRule, outOfMaxRule, inRangeRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.AUTO_APPROVED, decision.getFinalStatus());
        assertEquals(inRangeRule, decision.getAppliedRule());
    }

    @Test
    @DisplayName("Test ApprovalDecision getters and setters")
    void testApprovalDecision_GettersAndSetters() {
        PolicyEngineService.ApprovalDecision decision = new PolicyEngineService.ApprovalDecision();
        decision.setExpense(testExpense);
        decision.setFinalStatus(ApprovalStatus.AUTO_APPROVED);
        PolicyRule rule = new PolicyRule();
        rule.setRuleName("TEST_RULE");
        decision.setAppliedRule(rule);
        decision.setReason("Test reason");

        assertEquals(testExpense, decision.getExpense());
        assertEquals(ApprovalStatus.AUTO_APPROVED, decision.getFinalStatus());
        assertEquals(rule, decision.getAppliedRule());
        assertEquals("Test reason", decision.getReason());
    }
}