package com.bankingsystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for card transaction requests.
 */
@Data
public class CardTransactionRequest {
    
    @NotNull(message = "Card number is required")
    private String cardNumber;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Merchant name is required")
    private String merchantName;
    
    private String merchantCategory;
    
    private String location;
    
    private String transactionType = "PURCHASE";
    
    private String description;
    
    private Boolean isContactless = false;
    
    private Boolean isOnline = false;
    
    private Boolean isInternational = false;
}
