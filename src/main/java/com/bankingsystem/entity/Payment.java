package com.bankingsystem.entity;

import com.bankingsystem.enums.PaymentStatus;
import com.bankingsystem.enums.PaymentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity representing transfers and other payment types.
 */
@Entity
@Table(name = "payments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @NotNull(message = "Payment reference is required")
    @Column(name = "payment_reference", unique = true, nullable = false)
    private String paymentReference;

    @NotNull(message = "Payment type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @DecimalMin(value = "0.0", message = "Fee amount cannot be negative")
    @Column(name = "fee", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // External transfer fields
    @Size(max = 20, message = "External bank code cannot exceed 20 characters")
    @Column(name = "external_bank_code")
    private String externalBankCode;

    @Size(max = 50, message = "External account number cannot exceed 50 characters")
    @Column(name = "external_account_number")
    private String externalAccountNumber;

    @Size(max = 200, message = "Beneficiary name cannot exceed 200 characters")
    @Column(name = "beneficiary_name")
    private String beneficiaryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @PrePersist
    protected void onCreate() {
        if (scheduledDate == null) {
            scheduledDate = LocalDateTime.now();
        }
    }

    public boolean isCompleted() {
        return PaymentStatus.COMPLETED.equals(this.status);
    }

    public boolean isPending() {
        return PaymentStatus.PENDING.equals(this.status);
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(this.status);
    }

    public BigDecimal getTotalAmount() {
        return amount.add(fee != null ? fee : BigDecimal.ZERO);
    }
}
