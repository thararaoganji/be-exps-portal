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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    @DisplayName("Test 9: Immediate rejection when matched rule has REJECTED status")
    void testEvaluateExpense_RejectedStatusRule_ReturnsImmediately() {
        testExpense.setAmount(new BigDecimal("200.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule rejectRule = new PolicyRule();
        rejectRule.setRuleName("REJECT_RULE");
        rejectRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rejectRule.setConditionAmountMin(new BigDecimal("150.00"));
        rejectRule.setActionApprovalStatus(ApprovalStatus.REJECTED);
        rejectRule.setActionReason("Amount exceeds limit - rejected");
        rejectRule.setEnabled(true);
        rejectRule.setPriority(1);

        PolicyRule subsequentRule = new PolicyRule();
        subsequentRule.setRuleName("SUBSEQUENT_RULE");
        subsequentRule.setRuleType(PolicyRuleType.AUTO_APPROVE);
        subsequentRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        subsequentRule.setEnabled(true);
        subsequentRule.setPriority(2);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(rejectRule, subsequentRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED, result.getFinalStatus());
        assertEquals("Amount exceeds limit - rejected", result.getReason());
        assertEquals(rejectRule, result.getAppliedRule());
        assertEquals(testExpense, result.getExpense());
        verify(expenseApprovalRepository, times(1)).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 10: Category rule matches and applies action")
    void testEvaluateExpense_CategoryRule_Matches() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule categoryRule = new PolicyRule();
        categoryRule.setRuleName("MEALS_RULE");
        categoryRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        categoryRule.setConditionCategory(ExpenseCategory.MEALS);
        categoryRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        categoryRule.setActionReason("Meals category review");
        categoryRule.setEnabled(true);
        categoryRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Collections.singletonList(categoryRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());
        assertEquals(categoryRule, result.getAppliedRule());
        verify(expenseApprovalRepository, times(1)).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 11: Category rule does not match different category or null category")
    void testEvaluateExpense_CategoryRule_NonMatching() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.TRAVEL);

        PolicyRule categoryRule = new PolicyRule();
        categoryRule.setRuleName("MEALS_RULE");
        categoryRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        categoryRule.setConditionCategory(ExpenseCategory.MEALS);
        categoryRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        categoryRule.setEnabled(true);
        categoryRule.setPriority(1);

        PolicyRule nullCategoryRule = new PolicyRule();
        nullCategoryRule.setRuleName("NULL_CAT_RULE");
        nullCategoryRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        nullCategoryRule.setConditionCategory(null);
        nullCategoryRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        nullCategoryRule.setEnabled(true);
        nullCategoryRule.setPriority(2);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(categoryRule, nullCategoryRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
        verify(expenseApprovalRepository, never()).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 12: Age limit rule matches when expense is older than threshold")
    void testEvaluateExpense_AgeLimitRule_Matches() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(40));

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("AGE_LIMIT_RULE");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(30);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        ageRule.setActionReason("Expense older than 30 days");
        ageRule.setEnabled(true);
        ageRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Collections.singletonList(ageRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());
        verify(expenseApprovalRepository, times(1)).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 13: Age limit rule does not match when null or not older")
    void testEvaluateExpense_AgeLimitRule_NonMatching() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(10));

        PolicyRule nullAgeRule = new PolicyRule();
        nullAgeRule.setRuleName("NULL_AGE_RULE");
        nullAgeRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        nullAgeRule.setConditionAgeDays(null);
        nullAgeRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("AGE_LIMIT_RULE");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(30);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(nullAgeRule, ageRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
        verify(expenseApprovalRepository, never()).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 14: Receipt required rule with category filter matching and non-matching")
    void testEvaluateExpense_ReceiptRequired_WithCategoryFilter() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.TRAVEL);
        testExpense.setHasReceipt(false);

        PolicyRule nonMatchingCatRule = new PolicyRule();
        nonMatchingCatRule.setRuleName("MEALS_RECEIPT_REQUIRED");
        nonMatchingCatRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        nonMatchingCatRule.setConditionCategory(ExpenseCategory.MEALS);
        nonMatchingCatRule.setConditionReceiptRequired(true);
        nonMatchingCatRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        PolicyRule matchingCatRule = new PolicyRule();
        matchingCatRule.setRuleName("TRAVEL_RECEIPT_REQUIRED");
        matchingCatRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        matchingCatRule.setConditionCategory(ExpenseCategory.TRAVEL);
        matchingCatRule.setConditionReceiptRequired(true);
        matchingCatRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        matchingCatRule.setActionReason("Travel receipt missing");

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(nonMatchingCatRule, matchingCatRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());
        assertEquals("Travel receipt missing", result.getReason());
        verify(expenseApprovalRepository, times(1)).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test 15: Amount threshold evaluates min and max boundaries properly")
    void testEvaluateExpense_AmountThreshold_MinAndMaxBranches() {
        testExpense.setAmount(new BigDecimal("100.00"));

        PolicyRule outOfMinRangeRule = new PolicyRule();
        outOfMinRangeRule.setRuleName("MIN_TOO_HIGH");
        outOfMinRangeRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        outOfMinRangeRule.setConditionAmountMin(new BigDecimal("150.00"));

        PolicyRule outOfMaxRangeRule = new PolicyRule();
        outOfMaxRangeRule.setRuleName("MAX_TOO_LOW");
        outOfMaxRangeRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        outOfMaxRangeRule.setConditionAmountMin(new BigDecimal("50.00"));
        outOfMaxRangeRule.setConditionAmountMax(new BigDecimal("80.00"));

        PolicyRule inRangeRule = new PolicyRule();
        inRangeRule.setRuleName("IN_RANGE");
        inRangeRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        inRangeRule.setConditionAmountMin(new BigDecimal("50.00"));
        inRangeRule.setConditionAmountMax(new BigDecimal("150.00"));
        inRangeRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        inRangeRule.setActionReason("In range");

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(outOfMinRangeRule, outOfMaxRangeRule, inRangeRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());
        assertEquals("In range", result.getReason());
    }

    @Test
    @DisplayName("Test 16: Applied status with default/pending does not create approval records")
    void testEvaluateExpense_AppliedStatusPending_NoApprovalRecordCreated() {
        testExpense.setAmount(new BigDecimal("100.00"));

        PolicyRule pendingRule = new PolicyRule();
        pendingRule.setRuleName("PENDING_RULE");
        pendingRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        pendingRule.setConditionAmountMin(new BigDecimal("50.00"));
        pendingRule.setActionApprovalStatus(ApprovalStatus.PENDING);
        pendingRule.setActionReason("Requires standard pending review");

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Collections.singletonList(pendingRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
        assertEquals("Requires standard pending review", result.getReason());
        verify(expenseApprovalRepository, never()).save(any(ExpenseApproval.class));
    }
}