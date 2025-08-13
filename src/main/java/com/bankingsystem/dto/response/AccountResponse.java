package com.bankingsystem.dto.response;

import com.bankingsystem.enums.AccountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for account response.
 */
@Data
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal minimumBalance;
    private Boolean isActive;
    private Boolean isFrozen;
    private LocalDateTime openedDate;
    private LocalDateTime closedDate;
    private BigDecimal interestRate;
    private BigDecimal overdraftLimit;
    private BigDecimal monthlyMaintenanceFee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
