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
import java.time.LocalDateTime;

/**
 * CardTransaction entity representing card-based transactions.
 */
@Entity
@Table(name = "card_transactions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CardTransaction extends BaseEntity {

    @NotNull(message = "Transaction reference is required")
    @Column(name = "transaction_reference", unique = true, nullable = false)
    private String transactionReference;

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

    @Size(max = 200, message = "Merchant name cannot exceed 200 characters")
    @Column(name = "merchant_name")
    private String merchantName;

    @Size(max = 50, message = "Merchant category cannot exceed 50 characters")
    @Column(name = "merchant_category")
    private String merchantCategory;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @Size(max = 50, message = "Transaction type cannot exceed 50 characters")
    @Column(name = "transaction_type")
    private String transactionType; // PURCHASE, WITHDRAWAL, REFUND, etc.

    @Column(name = "authorization_code")
    private String authorizationCode;

    @Column(name = "is_contactless", nullable = false)
    private Boolean isContactless = false;

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @Column(name = "is_international", nullable = false)
    private Boolean isInternational = false;

    @Column(name = "exchange_rate", precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "original_amount", precision = 19, scale = 2)
    private BigDecimal originalAmount;

    @Size(max = 3, message = "Currency code must be 3 characters")
    @Column(name = "original_currency")
    private String originalCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

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
