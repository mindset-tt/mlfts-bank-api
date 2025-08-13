package com.bankingsystem.exception;

/**
 * Exception thrown for invalid transaction operations.
 */
public class InvalidTransactionException extends BankingSystemException {
    
    public InvalidTransactionException(String message) {
        super(message, "INVALID_TRANSACTION");
    }
    
    public InvalidTransactionException() {
        super("Invalid transaction operation", "INVALID_TRANSACTION");
    }
}
