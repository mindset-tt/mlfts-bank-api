package com.bankingsystem.enums;

/**
 * Enumeration for loan status.
 */
public enum LoanStatus {
    APPLIED("Applied"),
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    ACTIVE("Active"),
    PAID_OFF("Paid Off"),
    DEFAULTED("Defaulted");

    private final String displayName;

    LoanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
