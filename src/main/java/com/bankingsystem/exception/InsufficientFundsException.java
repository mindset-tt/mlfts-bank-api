package com.bankingsystem.exception;

/**
 * Exception thrown when there are insufficient funds for a transaction.
 */
public class InsufficientFundsException extends BankingSystemException {
    
    public InsufficientFundsException(String message) {
        super(message, "INSUFFICIENT_FUNDS");
    }
    
    public InsufficientFundsException() {
        super("Insufficient funds to complete the transaction", "INSUFFICIENT_FUNDS");
    }
}
