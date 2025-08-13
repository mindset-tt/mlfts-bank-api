package com.bankingsystem.entity;

import com.bankingsystem.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BillPayment entity representing bill payment transactions.
 */
@Entity
@Table(name = "bill_payments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillPayment extends BaseEntity {

    @NotNull(message = "Payment reference is required")
    @Column(name = "payment_reference", unique = true, nullable = false)
    private String paymentReference;

    @NotNull(message = "Biller code is required")
    @Size(max = 20, message = "Biller code cannot exceed 20 characters")
    @Column(name = "biller_code", nullable = false)
    private String billerCode;

    @NotNull(message = "Biller name is required")
    @Size(max = 100, message = "Biller name cannot exceed 100 characters")
    @Column(name = "biller_name", nullable = false)
    private String billerName;

    @NotNull(message = "Customer reference is required")
    @Size(max = 50, message = "Customer reference cannot exceed 50 characters")
    @Column(name = "customer_reference", nullable = false)
    private String customerReference;

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

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
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
