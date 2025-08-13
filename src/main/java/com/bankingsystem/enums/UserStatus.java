package com.bankingsystem.enums;

/**
 * Enumeration representing the various statuses a user can have in the banking system.
 */
public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    PENDING_VERIFICATION("Pending Verification"),
    BLOCKED("Blocked"),
    DELETED("Deleted");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
