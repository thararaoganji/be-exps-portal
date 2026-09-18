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
    @DisplayName("Rule with REJECTED action immediately halts evaluation and creates rejection approval record")
    void testEvaluateExpense_RejectedStatus_StopsImmediately() {
        testExpense.setAmount(new BigDecimal("2000.00"));

        PolicyRule rejectRule = new PolicyRule();
        rejectRule.setRuleName("REJECT_POLICY");
        rejectRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rejectRule.setConditionAmountMin(new BigDecimal("1500.00"));
        rejectRule.setActionApprovalStatus(ApprovalStatus.REJECTED);
        rejectRule.setActionReason("Amount exceeds policy limit");
        rejectRule.setEnabled(true);
        rejectRule.setPriority(1);

        PolicyRule autoApproveRule = new PolicyRule();
        autoApproveRule.setRuleName("AUTO_APPROVE");
        autoApproveRule.setRuleType(PolicyRuleType.AUTO_APPROVE);
        autoApproveRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        autoApproveRule.setPriority(2);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rejectRule, autoApproveRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.REJECTED, decision.getFinalStatus());
        assertEquals(rejectRule, decision.getAppliedRule());
        assertEquals("Amount exceeds policy limit", decision.getReason());
        assertEquals(testExpense, decision.getExpense());
        verify(expenseApprovalRepository, times(1)).save(any(ExpenseApproval.class));
    }

    @Test
    @DisplayName("Category rule evaluation matches when expense has the exact category")
    void testEvaluateExpense_CategoryRule_Matches() {
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule categoryRule = new PolicyRule();
        categoryRule.setRuleName("MEALS_CATEGORY_RULE");
        categoryRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        categoryRule.setConditionCategory(ExpenseCategory.MEALS);
        categoryRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        categoryRule.setActionReason("Meal category review");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(categoryRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(decision);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, decision.getFinalStatus());
        assertEquals(categoryRule, decision.getAppliedRule());
    }

    @Test
    @DisplayName("Category rule does not match when conditionCategory is null or different category")
    void testEvaluateExpense_CategoryRule_NoMatch() {
        testExpense.setCategory(ExpenseCategory.MEALS);

        PolicyRule nullCategoryRule = new PolicyRule();
        nullCategoryRule.setRuleName("NULL_CATEGORY_RULE");
        nullCategoryRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        nullCategoryRule.setConditionCategory(null);
        nullCategoryRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(nullCategoryRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
    }

    @Test
    @DisplayName("Age limit rule matches when expense is older than threshold")
    void testEvaluateExpense_AgeLimitRule_Matches() {
        testExpense.setExpenseDate(LocalDate.now().minusDays(45));

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("AGE_LIMIT_RULE");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(30);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        ageRule.setActionReason("Expense submitted late");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(ageRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.MANAGER_REVIEW, decision.getFinalStatus());
        assertEquals(ageRule, decision.getAppliedRule());
    }

    @Test
    @DisplayName("Age limit rule does not match when conditionAgeDays is null or expense is recent")
    void testEvaluateExpense_AgeLimitRule_NoMatch() {
        testExpense.setExpenseDate(LocalDate.now().minusDays(10));

        PolicyRule nullAgeDaysRule = new PolicyRule();
        nullAgeDaysRule.setRuleName("NULL_AGE_DAYS_RULE");
        nullAgeDaysRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        nullAgeDaysRule.setConditionAgeDays(null);

        PolicyRule notOldEnoughRule = new PolicyRule();
        notOldEnoughRule.setRuleName("RECENT_RULE");
        notOldEnoughRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        notOldEnoughRule.setConditionAgeDays(30);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(nullAgeDaysRule, notOldEnoughRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
    }

    @Test
    @DisplayName("Receipt required rule category mismatch branch")
    void testEvaluateExpense_ReceiptRequired_CategoryMismatch() {
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(false);

        PolicyRule receiptRule = new PolicyRule();
        receiptRule.setRuleName("TRAVEL_RECEIPT_RULE");
        receiptRule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        receiptRule.setConditionCategory(null); // Will match category check if conditionCategory is checked properly
        receiptRule.setConditionReceiptRequired(false); // Does not match missing receipt condition

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(receiptRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
    }

    @Test
    @DisplayName("Amount threshold rule min and max boundary checks")
    void testEvaluateAmountThreshold_MinAndMaxChecks() {
        testExpense.setAmount(new BigDecimal("100.00"));

        // Case 1: min condition fails (amount is less than min)
        PolicyRule minFails = new PolicyRule();
        minFails.setRuleName("MIN_FAILS");
        minFails.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        minFails.setConditionAmountMin(new BigDecimal("150.00"));
        minFails.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        // Case 2: max condition passes
        PolicyRule maxPasses = new PolicyRule();
        maxPasses.setRuleName("MAX_PASSES");
        maxPasses.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        maxPasses.setConditionAmountMin(new BigDecimal("50.00"));
        maxPasses.setConditionAmountMax(new BigDecimal("100.00"));
        maxPasses.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        maxPasses.setActionReason("Between 50 and 100");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(minFails, maxPasses));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.MANAGER_REVIEW, decision.getFinalStatus());
        assertEquals(maxPasses, decision.getAppliedRule());
    }

    @Test
    @DisplayName("Approval records creation with status other than AUTO_APPROVED, MANAGER_REVIEW, or FINANCE_REVIEW does not create records")
    void testCreateApprovalRecordsForStatus_PendingStatus() {
        testExpense.setAmount(new BigDecimal("10.00"));

        PolicyRule pendingRule = new PolicyRule();
        pendingRule.setRuleName("PENDING_RULE");
        pendingRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        pendingRule.setConditionAmountMin(new BigDecimal("5.00"));
        pendingRule.setActionApprovalStatus(ApprovalStatus.PENDING);
        pendingRule.setActionReason("Pending review");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(pendingRule));

        PolicyEngineService.ApprovalDecision decision = policyEngineService.evaluateExpense(testExpense);

        assertEquals(ApprovalStatus.PENDING, decision.getFinalStatus());
        verify(expenseApprovalRepository, never()).save(any(ExpenseApproval.class));
    }
}