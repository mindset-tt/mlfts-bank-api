package com.bankingsystem.enums;

/**
 * Enumeration for user roles in the banking system.
 */
public enum UserRole {
    CUSTOMER("Customer"),
    ADMIN("Administrator"),
    MANAGER("Bank Manager"),
    TELLER("Bank Teller"),
    LOAN_OFFICER("Loan Officer"),
    SECURITY_OFFICER("Security Officer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
