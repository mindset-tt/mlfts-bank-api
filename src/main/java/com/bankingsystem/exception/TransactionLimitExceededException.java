package com.bankingsystem.exception;

/**
 * Exception thrown when a transaction exceeds limits.
 */
public class TransactionLimitExceededException extends BankingSystemException {
    
    public TransactionLimitExceededException(String message) {
        super(message, "TRANSACTION_LIMIT_EXCEEDED");
    }
    
    public TransactionLimitExceededException() {
        super("Transaction amount exceeds the allowed limit", "TRANSACTION_LIMIT_EXCEEDED");
    }
}
