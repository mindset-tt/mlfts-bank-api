package com.bankingsystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for loan payment requests.
 */
@Data
public class LoanPaymentRequest {
    
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be positive")
    private BigDecimal paymentAmount;
    
    @NotNull(message = "Payment account ID is required")
    private Long paymentAccountId;
    
    private String paymentMethod = "ONLINE";
    
    private Boolean isAutoPayment = false;
    
    private String notes;
}
