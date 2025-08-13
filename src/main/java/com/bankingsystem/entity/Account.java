package com.bankingsystem.entity;

import com.bankingsystem.enums.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Account entity representing bank accounts.
 */
@Entity
@Table(name = "accounts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {

    @NotNull(message = "Account number is required")
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Available balance cannot be negative")
    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Minimum balance cannot be negative")
    @Column(name = "minimum_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_frozen", nullable = false)
    private Boolean isFrozen = false;

    @Column(name = "opened_date", nullable = false)
    private LocalDateTime openedDate;

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(name = "overdraft_limit", precision = 19, scale = 2)
    private BigDecimal overdraftLimit = BigDecimal.ZERO;

    @Column(name = "monthly_maintenance_fee", precision = 19, scale = 2)
    private BigDecimal monthlyMaintenanceFee = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "fromAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> outgoingTransactions = new HashSet<>();

    @OneToMany(mappedBy = "toAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> incomingTransactions = new HashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Card> cards = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (openedDate == null) {
            openedDate = LocalDateTime.now();
        }
        if (availableBalance == null) {
            availableBalance = balance;
        }
    }

    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (balance.subtract(amount).compareTo(minimumBalance.add(overdraftLimit).negate()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }
}
