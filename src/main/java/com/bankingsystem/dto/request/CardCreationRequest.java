package com.bankingsystem.dto.request;

import com.bankingsystem.enums.CardType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for card creation requests.
 */
@Data
public class CardCreationRequest {
    
    @NotNull(message = "Card type is required")
    private CardType cardType;
    
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @NotNull(message = "PIN is required")
    @Pattern(regexp = "\\d{4}", message = "PIN must be 4 digits")
    private String pin;
    
    @DecimalMin(value = "1000.00", message = "Minimum credit limit is $1,000")
    private BigDecimal creditLimit;
}
