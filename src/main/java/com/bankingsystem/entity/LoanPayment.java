package com.bankingsystem.entity;

import com.bankingsystem.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * LoanPayment entity representing loan payment transactions.
 */
@Entity
@Table(name = "loan_payments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LoanPayment extends BaseEntity {

    @NotNull(message = "Payment reference is required")
    @Column(name = "payment_reference", unique = true, nullable = false)
    private String paymentReference;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Column(name = "payment_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paymentAmount;

    @DecimalMin(value = "0.0", message = "Principal amount cannot be negative")
    @Column(name = "principal_amount", precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @DecimalMin(value = "0.0", message = "Interest amount cannot be negative")
    @Column(name = "interest_amount", precision = 19, scale = 2)
    private BigDecimal interestAmount;

    @DecimalMin(value = "0.0", message = "Fee amount cannot be negative")
    @Column(name = "fee_amount", precision = 19, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "is_overdue", nullable = false)
    private Boolean isOverdue = false;

    @Column(name = "overdue_days")
    private Integer overdueDays = 0;

    @DecimalMin(value = "0.0", message = "Late fee cannot be negative")
    @Column(name = "late_fee", precision = 19, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(name = "is_auto_payment", nullable = false)
    private Boolean isAutoPayment = false;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Size(max = 100, message = "Payment method cannot exceed 100 characters")
    @Column(name = "payment_method")
    private String paymentMethod;

    @DecimalMin(value = "0.0", message = "Remaining balance cannot be negative")
    @Column(name = "remaining_balance", precision = 19, scale = 2)
    private BigDecimal remainingBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_account_id")
    private Account paymentAccount;

    @PrePersist
    protected void onCreate() {
        if (isOverdue == null) {
            isOverdue = LocalDate.now().isAfter(dueDate);
        }
        if (isOverdue && overdueDays == null) {
            overdueDays = (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }
    }

    public boolean isCompleted() {
        return TransactionStatus.COMPLETED.equals(this.status);
    }

    public boolean isPending() {
        return TransactionStatus.PENDING.equals(this.status);
    }

    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.status);
    }

    public BigDecimal getTotalPayment() {
        BigDecimal total = paymentAmount;
        if (feeAmount != null) {
            total = total.add(feeAmount);
        }
        if (lateFee != null) {
            total = total.add(lateFee);
        }
        return total;
    }
}
