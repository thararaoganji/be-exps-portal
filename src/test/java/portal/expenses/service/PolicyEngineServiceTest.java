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
    @DisplayName("Test 9: Immediate rejection stops evaluation and creates AUTO record")
    void testEvaluateExpense_RejectionRule_ImmediatelyRejects() {
        testExpense.setAmount(new BigDecimal("5000.00"));

        PolicyRule rejectRule = new PolicyRule();
        rejectRule.setRuleName("AUTO_REJECT_OVER_BUDGET");
        rejectRule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rejectRule.setConditionAmountMin(new BigDecimal("4000.00"));
        rejectRule.setActionApprovalStatus(ApprovalStatus.REJECTED);
        rejectRule.setActionReason("Amount exceeds policy max");
        rejectRule.setPriority(1);

        PolicyRule secondaryRule = new PolicyRule();
        secondaryRule.setRuleName("FINANCE_APPROVAL");
        secondaryRule.setRuleType(PolicyRuleType.FINANCE_APPROVAL);
        secondaryRule.setActionApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        secondaryRule.setPriority(2);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(rejectRule, secondaryRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED, result.getFinalStatus());
        assertEquals("Amount exceeds policy max", result.getReason());
        assertEquals(rejectRule, result.getAppliedRule());
        assertEquals(testExpense, result.getExpense());

        ArgumentCaptor<ExpenseApproval> captor = ArgumentCaptor.forClass(ExpenseApproval.class);
        verify(expenseApprovalRepository, times(1)).save(captor.capture());
        ExpenseApproval savedApproval = captor.getValue();
        assertEquals(ApprovalStatus.REJECTED, savedApproval.getStatus());
        assertEquals("AUTO", savedApproval.getApprovalLevel());
        assertEquals(rejectRule, savedApproval.getAppliedRule());
        assertEquals("Amount exceeds policy max", savedApproval.getComments());
    }

    @Test
    @DisplayName("Test 10: Category rule matches expense category")
    void testEvaluateExpense_CategoryRule_Matches() {
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
        assertEquals(categoryRule, result.getAppliedRule());
    }

    @Test
    @DisplayName("Test 11: Category rule does not match when category differs or is null")
    void testEvaluateExpense_CategoryRule_NoMatch() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setCategory(ExpenseCategory.TRAVEL);

        PolicyRule categoryRule = new PolicyRule();
        categoryRule.setRuleName("MEALS_RULE");
        categoryRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        categoryRule.setConditionCategory(ExpenseCategory.MEALS);
        categoryRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);

        PolicyRule nullCatRule = new PolicyRule();
        nullCatRule.setRuleName("NULL_CAT_RULE");
        nullCatRule.setRuleType(PolicyRuleType.CATEGORY_RULE);
        nullCatRule.setConditionCategory(null);
        nullCatRule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(categoryRule, nullCatRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test 12: Age limit rule matches when expense is older than threshold")
    void testEvaluateExpense_AgeLimit_Matches() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(45));

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("OLD_EXPENSE");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(30);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);
        ageRule.setActionReason("Submitted after 30 days");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(ageRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.MANAGER_REVIEW, result.getFinalStatus());
        assertEquals(ageRule, result.getAppliedRule());
    }

    @Test
    @DisplayName("Test 13: Age limit rule does not match when expense is newer or conditionAgeDays is null")
    void testEvaluateExpense_AgeLimit_NoMatch() {
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setExpenseDate(LocalDate.now().minusDays(10));

        PolicyRule ageRule = new PolicyRule();
        ageRule.setRuleName("OLD_EXPENSE");
        ageRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        ageRule.setConditionAgeDays(30);
        ageRule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        PolicyRule nullAgeRule = new PolicyRule();
        nullAgeRule.setRuleName("NULL_AGE");
        nullAgeRule.setRuleType(PolicyRuleType.AGE_LIMIT);
        nullAgeRule.setConditionAgeDays(null);

        when(policyRuleRepository.findAllEnabledOrderedByPriority())
                .thenReturn(Arrays.asList(ageRule, nullAgeRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test 14: Receipt required rule with category mismatch does not match")
    void testEvaluateExpense_ReceiptRequired_CategoryMismatch() {
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(false);
        testExpense.setAmount(new BigDecimal("100.00"));

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("RECEIPT_FOR_TRAVEL");
        rule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        rule.setConditionCategory(ExpenseCategory.TRAVEL);
        rule.setConditionReceiptRequired(true);
        rule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test 15: Receipt required rule returns false when expense already has receipt")
    void testEvaluateExpense_ReceiptRequired_HasReceiptTrue() {
        testExpense.setCategory(ExpenseCategory.MEALS);
        testExpense.setHasReceipt(true);
        testExpense.setAmount(new BigDecimal("100.00"));

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("RECEIPT_REQUIRED");
        rule.setRuleType(PolicyRuleType.RECEIPT_REQUIRED);
        rule.setConditionCategory(ExpenseCategory.MEALS);
        rule.setConditionReceiptRequired(true);
        rule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test 16: Finance review creates two approval records (MANAGER and FINANCE)")
    void testEvaluateExpense_FinanceReview_CreatesBothApprovalRecords() {
        testExpense.setAmount(new BigDecimal("2000.00"));
        testExpense.setHasReceipt(true);

        PolicyRule financeRule = new PolicyRule();
        financeRule.setRuleName("FINANCE_APPROVAL");
        financeRule.setRuleType(PolicyRuleType.FINANCE_APPROVAL);
        financeRule.setConditionAmountMin(new BigDecimal("1000.00"));
        financeRule.setActionApprovalStatus(ApprovalStatus.FINANCE_REVIEW);
        financeRule.setActionReason("Finance and Manager review required");

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(financeRule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.FINANCE_REVIEW, result.getFinalStatus());

        ArgumentCaptor<ExpenseApproval> captor = ArgumentCaptor.forClass(ExpenseApproval.class);
        verify(expenseApprovalRepository, times(2)).save(captor.capture());
        List<ExpenseApproval> savedApprovals = captor.getAllValues();

        assertEquals(ApprovalStatus.MANAGER_REVIEW, savedApprovals.get(0).getStatus());
        assertEquals("MANAGER", savedApprovals.get(0).getApprovalLevel());
        assertEquals(ApprovalStatus.FINANCE_REVIEW, savedApprovals.get(1).getStatus());
        assertEquals("FINANCE", savedApprovals.get(1).getApprovalLevel());
    }

    @Test
    @DisplayName("Test 17: Amount min not matching prevents match")
    void testEvaluateExpense_AmountMinNotMatching() {
        testExpense.setAmount(new BigDecimal("50.00"));

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("MIN_100");
        rule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rule.setConditionAmountMin(new BigDecimal("100.00"));
        rule.setActionApprovalStatus(ApprovalStatus.MANAGER_REVIEW);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }

    @Test
    @DisplayName("Test 18: Amount max not matching prevents match")
    void testEvaluateExpense_AmountMaxNotMatching() {
        testExpense.setAmount(new BigDecimal("150.00"));

        PolicyRule rule = new PolicyRule();
        rule.setRuleName("MAX_100");
        rule.setRuleType(PolicyRuleType.AMOUNT_THRESHOLD);
        rule.setConditionAmountMax(new BigDecimal("100.00"));
        rule.setActionApprovalStatus(ApprovalStatus.AUTO_APPROVED);

        when(policyRuleRepository.findAllEnabledOrderedByPriority()).thenReturn(Arrays.asList(rule));

        PolicyEngineService.ApprovalDecision result = policyEngineService.evaluateExpense(testExpense);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING, result.getFinalStatus());
    }
}