package com.bankingsystem.entity;

import com.bankingsystem.enums.CardStatus;
import com.bankingsystem.enums.CardType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
 * Card entity representing credit and debit cards.
 */
@Entity
@Table(name = "cards")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Card extends BaseEntity {

    @NotNull(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be 16 digits")
    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;

    @NotNull(message = "Card type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @NotNull(message = "Card status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.PENDING_ACTIVATION;

    @NotNull(message = "Cardholder name is required")
    @Size(max = 100, message = "Cardholder name cannot exceed 100 characters")
    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;

    @NotNull(message = "Expiry date is required")
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @NotNull(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    @Column(nullable = false)
    private String cvv;

    @NotNull(message = "PIN is required")
    @Size(min = 4, max = 6, message = "PIN must be between 4 and 6 characters")
    @Column(nullable = false)
    private String pin;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Column(name = "activated_date")
    private LocalDateTime activatedDate;

    @Column(name = "blocked_date")
    private LocalDateTime blockedDate;

    @DecimalMin(value = "0.0", message = "Daily limit cannot be negative")
    @Column(name = "daily_limit", precision = 19, scale = 2)
    private BigDecimal dailyLimit;

    @DecimalMin(value = "0.0", message = "Monthly limit cannot be negative")
    @Column(name = "monthly_limit", precision = 19, scale = 2)
    private BigDecimal monthlyLimit;

    @DecimalMin(value = "0.0", message = "Credit limit cannot be negative")
    @Column(name = "credit_limit", precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @DecimalMin(value = "0.0", message = "Available credit cannot be negative")
    @Column(name = "available_credit", precision = 19, scale = 2)
    private BigDecimal availableCredit;

    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(name = "contactless_enabled", nullable = false)
    private Boolean contactlessEnabled = true;

    @Column(name = "online_transactions_enabled", nullable = false)
    private Boolean onlineTransactionsEnabled = true;

    @Column(name = "international_transactions_enabled", nullable = false)
    private Boolean internationalTransactionsEnabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CardTransaction> cardTransactions = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (issuedDate == null) {
            issuedDate = LocalDateTime.now();
        }
        if (cardType == CardType.CREDIT && availableCredit == null) {
            availableCredit = creditLimit;
        }
    }

    public boolean isActive() {
        return CardStatus.ACTIVE.equals(this.status);
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(this.expiryDate);
    }

    public boolean canMakeTransaction(BigDecimal amount) {
        if (!isActive() || isExpired()) {
            return false;
        }
        
        if (cardType == CardType.CREDIT) {
            return availableCredit.compareTo(amount) >= 0;
        }
        
        return account.getAvailableBalance().compareTo(amount) >= 0;
    }
}
