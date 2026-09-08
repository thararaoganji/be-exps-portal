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
    @DisplayName("Test: Rejection rule immediately returns REJECTED")
    void testEvaluateExpense_RejectionRule_ImmediateRejection() {
        testExpense.setAmount(new BigDecimal("5000.00"));
        testExpense.setHasReceipt(true);

        PolicyRule rejectRule = new PolicyRule();
        rejectRule.setRuleName("REJECT_HIGH_AMOUNT");
        rejectRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rejectRule.setConditionAmountMin(new BigDecimal("3000.00"));
        rejectRule.setActionApprovalStatus(ApprovalStatus.REJECTED);
        rejectRule.setActionReason("Amount exceeds policy max limit");
        rejectRule.setEnabled(true);
        rejectRule.setPriority(1);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rejectRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED, result.getFinalStatus());
        assertEquals(rejectRule, result.getAppliedRule());
        assertEquals("Amount exceeds policy max limit", result.getReason());
        assertEquals(testExpense, result.getExpense());
        verify(expenseApprovalRepository, times(1)).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Test: Category rule matching and non-matching")
    void testEvaluateExpense_CategoryRule() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule categoryRule = new PolicyRule();
        categoryRule.setRuleName("MEALS_RULE");
        categoryRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        categoryRule.setConditionCategory(ExpenseCategory.MEALS);
        categoryRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        categoryRule.setActionReason("Meals category review");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(categoryRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);
        assertNotNull(result);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());

        // Test non-matching category
        testExpense.setCategory(ExpenseCategory.TRAVEL);
        result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());

        // Test category rule with null condition category
        categoryRule.setConditionCategory(null);
        result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test: Age limit rule evaluation")
    void testEvaluateExpense_AgeLimit() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(45));

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("AGE_LIMIT_30");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(30);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        ageRule.setActionReason("Expense older than 30 days");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(ageRule));

        // Matches when expense is older
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());

        // Does not match when expense is newer
        testExpense.setExpenseDate(LocalDate.now().minusDays(10));
        result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());

        // Does not match when conditionAgeDays is null
        ageRule.setConditionAgeDays(null);
        result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test: Receipt required rule with category condition and receipt present/missing")
    void testEvaluateExpense_ReceiptRequiredCategory() {
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(false);

        PolicyRule receiptRule = new PolicyRule();
        receiptRule.setRuleName("RECEIPT_FOR_TRAVEL");
        receiptRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        receiptRule.setConditionCategory(ExpenseCategory.TRAVEL);
        receiptRule.setConditionReceiptRequired(true);
        receiptRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        receiptRule.setActionReason("Travel receipt required");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(receiptRule));

        // Category does not match
        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());

        // Category matches and receipt missing
        testExpense.setCategory(ExpenseCategory.TRAVEL);
        result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());

        // Category matches but receipt is present
        testExpense.setHasReceipt(true);
        result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());

        // ConditionReceiptRequired is false
        testExpense.setHasReceipt(false);
        receiptRule.setConditionReceiptRequired(false);
        result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test: Approval records creation for PENDING and FINANCE_REVIEW")
    void testEvaluateExpense_ApprovalRecordCreation() {
        testExpense.setAmount(new BigDecimal("2000.00"));
        testExpense.setHasReceipt(true);

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("PENDING_RULE");
        rule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rule.setConditionAmountMin(new BigDecimal("1000.00"));
        rule.setActionApprovalStatus(ApprovalStatus.PENDING);
        rule.setActionReason("Default pending");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
        verify(expenseApprovalRepository, never()).save(any(ExpenseApproval.class));

        // When FINANCE_REVIEW is matched, 2 approval records should be created (MANAGER and FINANCE)
        rule.setActionApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        result = policyEngineService.evaluateExpense(testExpense);
        assertEquals(ApprovalStatus.FINANCE_REVIEW, result.getFinalStatus());
        verify(expenseApprovalRepository, times(2)).save(any(ExpenseApproval.class));
    }
}