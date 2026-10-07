package portal.expenses.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "policy_rules")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class PolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, unique = true)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private PolicyRuleType ruleType;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "priority")
    private Integer priority = 0;

    // Condition fields
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_category")
    private ExpenseCategory conditionCategory;

    @Column(name = "condition_amount_min")
    private BigDecimal conditionAmountMin;

    @Column(name = "condition_amount_max")
    private BigDecimal conditionAmountMax;

    @Column(name = "condition_age_days")
    private Integer conditionAgeDays;

    @Column(name = "condition_receipt_required")
    private Boolean conditionReceiptRequired;

    // Action fields
    @Enumerated(EnumType.STRING)
    @Column(name = "action_approval_status", nullable = false)
    private ApprovalStatus actionApprovalStatus;

    @Column(name = "action_reason", columnDefinition = "TEXT")
    private String actionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}