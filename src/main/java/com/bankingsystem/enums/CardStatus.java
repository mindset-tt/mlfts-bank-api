package com.bankingsystem.enums;

/**
 * Enumeration for card status.
 */
public enum CardStatus {
    ACTIVE("Active"),
    BLOCKED("Blocked"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled"),
    PENDING_ACTIVATION("Pending Activation");

    private final String displayName;

    CardStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
