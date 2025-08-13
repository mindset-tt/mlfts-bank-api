package com.bankingsystem.dto.request;

import com.bankingsystem.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for account creation request.
 */
@Data
public class AccountCreationRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @DecimalMin(value = "0.0", message = "Initial balance cannot be negative")
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Overdraft limit cannot be negative")
    private BigDecimal overdraftLimit = BigDecimal.ZERO;

    private String purpose;
}
