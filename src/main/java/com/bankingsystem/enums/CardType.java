package com.bankingsystem.enums;

/**
 * Enumeration for card types.
 */
public enum CardType {
    DEBIT("Debit Card"),
    CREDIT("Credit Card"),
    PREPAID("Prepaid Card");

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
