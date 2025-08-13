package com.bankingsystem.exception;

/**
 * Exception thrown when an account is frozen or inactive.
 */
public class AccountFrozenException extends BankingSystemException {
    
    public AccountFrozenException(String message) {
        super(message, "ACCOUNT_FROZEN");
    }
    
    public AccountFrozenException() {
        super("Account is frozen and cannot perform transactions", "ACCOUNT_FROZEN");
    }
}
