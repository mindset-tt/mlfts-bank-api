package com.bankingsystem.dto.payment;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for bill payments.
 */
@Data
public class BillPaymentRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Biller code is required")
    private String billerCode;

    @NotBlank(message = "Biller name is required")
    @Size(min = 2, max = 100, message = "Biller name must be between 2 and 100 characters")
    private String billerName;

    @NotBlank(message = "Customer reference is required")
    private String customerReference;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @DecimalMax(value = "50000.00", message = "Amount exceeds bill payment limit")
    private BigDecimal amount;
}
