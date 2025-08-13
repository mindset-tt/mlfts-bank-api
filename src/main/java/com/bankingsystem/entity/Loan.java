package com.bankingsystem.entity;

import com.bankingsystem.enums.LoanStatus;
import com.bankingsystem.enums.LoanType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Loan entity representing different types of loans.
 */
@Entity
@Table(name = "loans")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Loan extends BaseEntity {

    @NotNull(message = "Loan number is required")
    @Column(name = "loan_number", unique = true, nullable = false)
    private String loanNumber;

    @NotNull(message = "Loan type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;

    @NotNull(message = "Loan status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.APPLIED;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.0", message = "Principal amount must be at least 1000")
    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be greater than 0")
    @Max(value = 50, message = "Interest rate cannot exceed 50%")
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal interestRate;

    @NotNull(message = "Term in months is required")
    @Min(value = 1, message = "Term must be at least 1 month")
    @Max(value = 480, message = "Term cannot exceed 480 months (40 years)")
    @Column(name = "term_in_months", nullable = false)
    private Integer termInMonths;

    @DecimalMin(value = "0.0", message = "Monthly payment cannot be negative")
    @Column(name = "monthly_payment", precision = 19, scale = 2)
    private BigDecimal monthlyPayment;

    @DecimalMin(value = "0.0", message = "Outstanding balance cannot be negative")
    @Column(name = "outstanding_balance", precision = 19, scale = 2)
    private BigDecimal outstandingBalance;

    @DecimalMin(value = "0.0", message = "Total interest cannot be negative")
    @Column(name = "total_interest", precision = 19, scale = 2)
    private BigDecimal totalInterest;

    @DecimalMin(value = "0.0", message = "Total amount cannot be negative")
    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "disbursement_date")
    private LocalDateTime disbursementDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Size(max = 1000, message = "Purpose cannot exceed 1000 characters")
    private String purpose;

    @Size(max = 2000, message = "Collateral description cannot exceed 2000 characters")
    @Column(name = "collateral_description")
    private String collateralDescription;

    @DecimalMin(value = "0.0", message = "Collateral value cannot be negative")
    @Column(name = "collateral_value", precision = 19, scale = 2)
    private BigDecimal collateralValue;

    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 850, message = "Credit score cannot exceed 850")
    @Column(name = "credit_score")
    private Integer creditScore;

    @DecimalMin(value = "0.0", message = "Annual income cannot be negative")
    @Column(name = "annual_income", precision = 19, scale = 2)
    private BigDecimal annualIncome;

    @DecimalMin(value = "0.0", message = "Debt to income ratio cannot be negative")
    @Column(name = "debt_to_income_ratio", precision = 5, scale = 4)
    private BigDecimal debtToIncomeRatio;

    @Column(name = "is_secured", nullable = false)
    private Boolean isSecured = false;

    @Column(name = "auto_payment_enabled", nullable = false)
    private Boolean autoPaymentEnabled = false;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursement_account_id")
    private Account disbursementAccount;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LoanPayment> loanPayments = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (applicationDate == null) {
            applicationDate = LocalDateTime.now();
        }
        if (outstandingBalance == null) {
            outstandingBalance = principalAmount;
        }
    }

    public boolean isActive() {
        return LoanStatus.ACTIVE.equals(this.status);
    }

    public boolean isApproved() {
        return LoanStatus.APPROVED.equals(this.status);
    }

    public boolean isPaidOff() {
        return LoanStatus.PAID_OFF.equals(this.status);
    }

    public BigDecimal getRemainingBalance() {
        return outstandingBalance != null ? outstandingBalance : principalAmount;
    }
}
