package com.bankingsystem.dto.response;

import com.bankingsystem.enums.TransactionStatus;
import com.bankingsystem.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transaction response.
 */
@Data
public class TransactionResponse {
    
    private Long id;
    private String transactionReference;
    private TransactionType transactionType;
    private BigDecimal amount;
    private TransactionStatus status;
    private String description;
    private LocalDateTime transactionDate;
    private LocalDateTime processedDate;
    private BigDecimal runningBalance;
    private String channel;
    private String notes;
    private BigDecimal feeAmount;
    
    // Account information
    private Long fromAccountId;
    private String fromAccountNumber;
    private Long toAccountId;
    private String toAccountNumber;
    
    // User information
    private Long initiatedByUserId;
    private String initiatedByUsername;
}
