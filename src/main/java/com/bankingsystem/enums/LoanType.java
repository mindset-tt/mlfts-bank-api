package com.bankingsystem.enums;

/**
 * Enumeration for loan types.
 */
public enum LoanType {
    PERSONAL("Personal Loan"),
    HOME("Home Loan"),
    AUTO("Auto Loan"),
    BUSINESS("Business Loan"),
    EDUCATION("Education Loan"),
    CREDIT_LINE("Line of Credit");

    private final String displayName;

    LoanType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
