package com.bankingsystem.exception;

/**
 * Exception thrown when attempting to create a user that already exists in the system.
 */
public class UserAlreadyExistsException extends BankingSystemException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAlreadyExistsException(String field, String value) {
        super(String.format("User with %s '%s' already exists", field, value));
    }
}
