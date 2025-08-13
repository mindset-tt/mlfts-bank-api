package com.bankingsystem.entity;

import com.bankingsystem.enums.TransactionStatus;
import com.bankingsystem.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity representing all financial transactions.
 */
@Entity
@Table(name = "transactions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @NotNull(message = "Transaction reference is required")
    @Column(name = "transaction_reference", unique = true, nullable = false)
    private String transactionReference;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Transaction status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "running_balance", precision = 19, scale = 2)
    private BigDecimal runningBalance;

    @Size(max = 100, message = "Channel cannot exceed 100 characters")
    private String channel; // Online, ATM, Branch, Mobile, etc.

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Column(name = "fee_amount", precision = 19, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by_user_id")
    private User initiatedBy;

    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
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
}
