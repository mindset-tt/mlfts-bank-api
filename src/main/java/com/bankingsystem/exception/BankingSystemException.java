package com.bankingsystem.exception;

/**
 * Base exception class for all banking system exceptions.
 */
public class BankingSystemException extends RuntimeException {
    
    private final String errorCode;
    
    public BankingSystemException(String message) {
        super(message);
        this.errorCode = "BANKING_ERROR";
    }
    
    public BankingSystemException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BankingSystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BANKING_ERROR";
    }
    
    public BankingSystemException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
