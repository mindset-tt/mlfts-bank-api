package com.bankingsystem.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class for generating various types of numbers and references.
 */
public class NumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    
    private static final String ACCOUNT_PREFIX = "ACC";
    private static final String CARD_PREFIX = "CARD";
    private static final String LOAN_PREFIX = "LOAN";
    private static final String TRANSACTION_PREFIX = "TXN";
    private static final String PAYMENT_PREFIX = "PAY";
    
    /**
     * Generate a unique account number.
     */
    public static String generateAccountNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomDigits = String.format("%06d", RANDOM.nextInt(1000000));
        return ACCOUNT_PREFIX + timestamp + randomDigits;
    }
    
    /**
     * Generate a unique card number (16 digits).
     */
    public static String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();
        cardNumber.append("4"); // Visa prefix
        for (int i = 0; i < 15; i++) {
            cardNumber.append(RANDOM.nextInt(10));
        }
        return cardNumber.toString();
    }
    
    /**
     * Generate a CVV (3 digits).
     */
    public static String generateCVV() {
        return String.format("%03d", RANDOM.nextInt(1000));
    }
    
    /**
     * Generate a card PIN (4 digits).
     */
    public static String generateCardPIN() {
        return String.format("%04d", RANDOM.nextInt(10000));
    }
    
    /**
     * Generate a PIN (4 digits).
     */
    public static String generatePIN() {
        return String.format("%04d", RANDOM.nextInt(10000));
    }
    
    /**
     * Generate a unique loan number.
     */
    public static String generateLoanNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomDigits = String.format("%08d", RANDOM.nextInt(100000000));
        return LOAN_PREFIX + timestamp + randomDigits;
    }
    
    /**
     * Generate a unique transaction reference.
     */
    public static String generateTransactionReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomDigits = String.format("%06d", RANDOM.nextInt(1000000));
        return TRANSACTION_PREFIX + timestamp + randomDigits;
    }
    
    /**
     * Generate a unique payment reference.
     */
    public static String generatePaymentReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomDigits = String.format("%06d", RANDOM.nextInt(1000000));
        return PAYMENT_PREFIX + timestamp + randomDigits;
    }
    
    /**
     * Generate a unique reference using UUID.
     */
    public static String generateUniqueReference() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
    
    /**
     * Generate a random string of specified length.
     */
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(RANDOM.nextInt(characters.length())));
        }
        return result.toString();
    }
    
    /**
     * Generate an authorization code for card transactions.
     */
    public static String generateAuthorizationCode() {
        return generateRandomString(6);
    }
    
    /**
     * Generate OTP (One Time Password).
     */
    public static String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(RANDOM.nextInt(10));
        }
        return otp.toString();
    }
}
