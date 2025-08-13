package com.bankingsystem.dto.response;

import com.bankingsystem.enums.TransactionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for card transaction responses.
 */
@Data
public class CardTransactionResponse {
    
    private Long id;
    private BigDecimal amount;
    private String merchantName;
    private String merchantCategory;
    private String location;
    private LocalDateTime transactionDate;
    private String transactionType;
    private String description;
    private TransactionStatus status;
    private String transactionReference;
    private String authorizationCode;
    private Boolean isContactless;
    private Boolean isOnline;
    private Boolean isInternational;
}
