package com.bankingsystem.enums;

/**
 * Enumeration for different types of bank accounts.
 */
public enum AccountType {
    CHECKING("Checking Account"),
    SAVINGS("Savings Account"),
    BUSINESS("Business Account"),
    INVESTMENT("Investment Account"),
    MONEY_MARKET("Money Market Account");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
