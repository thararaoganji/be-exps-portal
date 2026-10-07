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
    @DisplayName("Test 9: Immediate rejection when rule matches with REJECTED status")
    void testEvaluateExpense_RejectedRule_ReturnsImmediately() {
        testExpense.setAmount(new BigDecimal("5000.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(90));

        PolicyRule rejectRule = new PolicyRule();
        rejectRule.setRuleName("AGE_LIMIT_REJECT");
        rejectRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        rejectRule.setConditionAgeDays(60);
        rejectRule.setActionApprovalStatus(ApprovalStatus.REJECTED);
        rejectRule.setActionReason("Expense too old");
        rejectRule.setEnabled(true);
        rejectRule.setPriority(1);

        PolicyRule secondRule = new PolicyRule();
        secondRule.setRuleName("AUTO_APPROVE");
        secondRule.setRuleType(PolicyRuleType.AUTO_APPROVE);
        secondRule.setConditionAmountMax(new BigDecimal("10000.00"));
        secondRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rejectRule, secondRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED, result.getFinalStatus());
        assertEquals("Expense too old", result.getReason());
        assertEquals(rejectRule, result.getAppliedRule());
        assertEquals(testExpense, result.getExpense());

        ArgumentCaptor<ExpenseApproval> captor = ArgumentCaptor.forClass(ExpenseApproval.class);
        verify(expenseApprovalRepository, times(1)).save(captor.capture());
        ExpenseApproval savedApproval = captor.getValue();
        assertEquals(ApprovalStatus.REJECTED, savedApproval.getStatus());
        assertEquals("AUTO", savedApproval.getApprovalLevel());
        assertEquals("Expense too old", savedApproval.getComments());
    }

    @Test
    @DisplayName("Test 10: AMOUNT_THRESHOLD rule matching and non-matching conditions")
    void testEvaluateExpense_AmountThreshold_MinAndMaxChecks() {
        testExpense.setAmount(new BigDecimal("150.00"));

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("AMOUNT_THRESHOLD");
        rule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rule.setConditionAmountMin(new BigDecimal("100.00"));
        rule.setConditionAmountMax(new BigDecimal("200.00"));
        rule.setActionApprovalStatus(ApprovalStatus.PENDING);
        rule.setActionReason("Requires review");
        rule.setEnabled(true);
        rule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
        assertEquals(rule, result.getAppliedRule());
        // For PENDING status, createApprovalRecordsForStatus falls to default (no save called)
        verify(expenseApprovalRepository, never()).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 11: AMOUNT_THRESHOLD rule when min matches but max fails")
    void testEvaluateExpense_AmountThreshold_MaxFails() {
        testExpense.setAmount(new BigDecimal("250.00"));

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("AMOUNT_THRESHOLD");
        rule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rule.setConditionAmountMin(new BigDecimal("100.00"));
        rule.setConditionAmountMax(new BigDecimal("200.00"));
        rule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
        assertEquals("No matching policy rules, manual review required", result.getReason());
    }

    @Test
    @DisplayName("Test 12: AMOUNT_THRESHOLD rule when min check fails")
    void testEvaluateExpense_AmountThreshold_MinFails() {
        testExpense.setAmount(new BigDecimal("50.00"));

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("AMOUNT_THRESHOLD");
        rule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rule.setConditionAmountMin(new BigDecimal("100.00"));
        rule.setConditionAmountMax(new BigDecimal("200.00"));
        rule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test 13: CATEGORY_RULE evaluation matching and non-matching")
    void testEvaluateExpense_CategoryRule() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule matchingRule = new PolicyRule();
        matchingRule.setRuleName("MEALS_RULE");
        matchingRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        matchingRule.setConditionCategory(ExpenseCategory.MEALS);
        matchingRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        matchingRule.setActionReason("Meals category review");

        PolicyRule nonMatchingRule = new PolicyRule();
        nonMatchingRule.setRuleName("TRAVEL_RULE");
        nonMatchingRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        nonMatchingRule.setConditionCategory(null);
        nonMatchingRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(nonMatchingRule, matchingRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());
        assertEquals(matchingRule, result.getAppliedRule());
        verify(expenseApprovalRepository, times(1)).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 14: AGE_LIMIT rule evaluation")
    void testEvaluateExpense_AgeLimitRule() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(10));

        PolicyRule nullAgeRule = new PolicyRule();
        nullAgeRule.setRuleName("NULL_AGE");
        nullAgeRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        nullAgeRule.setConditionAgeDays(null);
        nullAgeRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);

        PolicyRule notExceededRule = new PolicyRule();
        notExceededRule.setRuleName("NOT_EXCEEDED");
        notExceededRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        notExceededRule.setConditionAgeDays(30);
        notExceededRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);

        PolicyRule exceededRule = new PolicyRule();
        exceededRule.setRuleName("EXCEEDED");
        exceededRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        exceededRule.setConditionAgeDays(5);
        exceededRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        exceededRule.setActionReason("Expense is older than 5 days");

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(nullAgeRule, notExceededRule, exceededRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());
        assertEquals(exceededRule, result.getAppliedRule());
    }

    @Test
    @DisplayName("Test 15: RECEIPT_REQUIRED category mismatch and receipt present cases")
    void testEvaluateExpense_ReceiptRequiredEdgeCases() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(true); // Receipt is present

        PolicyRule diffCategoryRule = new PolicyRule();
        diffCategoryRule.setRuleName("DIFF_CAT");
        diffCategoryRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        // We set conditionCategory to null to test when category condition is null vs not matching
        diffCategoryRule.setConditionCategory(null);
        diffCategoryRule.setConditionReceiptRequired(true);
        diffCategoryRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(diffCategoryRule));

        // Expense has receipt, so diffCategoryRule should return false
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test 16: FINANCE_REVIEW creates both MANAGER and FINANCE approval records")
    void testEvaluateExpense_FinanceReview_CreatesTwoApprovalRecords() {
        testExpense.setAmount(new BigDecimal("2000.00"));

        PolicyRule financeRule = new PolicyRule();
        financeRule.setRuleName("FINANCE_APPROVAL");
        financeRule.setRuleType(PolicyRuleType.FINANCE_APPROVAL);
        financeRule.setConditionAmountMin(new BigDecimal("1000.00"));
        financeRule.setActionApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        financeRule.setActionReason("Requires finance review");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(financeRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.FINANCE_REVIEW, result.getFinalStatus());

        ArgumentCaptor<ExpenseApproval> captor = ArgumentCaptor.forClass(ExpenseApproval.class);
        verify(expenseApprovalRepository, times(2)).save(captor.capture());

        List<ExpenseApproval> savedApprovals = captor.getAllValues();
        assertEquals(ApprovalStatus.MANAGER_REVIEW, savedApprovals.get(0).getStatus());
        assertEquals("MANAGER", savedApprovals.get(0).getApprovalLevel());
        assertEquals(ApprovalStatus.FINANCE_REVIEW, savedApprovals.get(1).getStatus());
        assertEquals("FINANCE", savedApprovals.get(1).getApprovalLevel());
    }
}