package com.bankingsystem.dto.response;

import com.bankingsystem.enums.CardStatus;
import com.bankingsystem.enums.CardType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for card responses.
 */
@Data
public class CardResponse {
    
    private Long id;
    private String cardNumber;
    private CardType cardType;
    private String cardholderName;
    private LocalDate expiryDate;
    private CardStatus status;
    private LocalDateTime issuedDate;
    private LocalDateTime activatedDate;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private Boolean contactlessEnabled;
    private Boolean onlineTransactionsEnabled;
    private Boolean internationalTransactionsEnabled;
}
