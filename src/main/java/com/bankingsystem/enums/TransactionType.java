package com.bankingsystem.enums;

/**
 * Enumeration for different types of transactions.
 */
public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER("Transfer"),
    PAYMENT("Payment"),
    FEE("Fee"),
    INTEREST("Interest"),
    LOAN_PAYMENT("Loan Payment"),
    CARD_PAYMENT("Card Payment"),
    REFUND("Refund"),
    CHARGE("Charge");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
