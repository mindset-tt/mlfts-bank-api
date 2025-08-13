package com.bankingsystem.dto.payment;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for external bank transfers.
 */
@Data
public class ExternalTransferRequest {

    @NotBlank(message = "From account number is required")
    private String fromAccountNumber;

    @NotBlank(message = "To account number is required")
    private String toAccountNumber;

    @NotBlank(message = "Bank code is required")
    private String bankCode;

    @NotBlank(message = "Beneficiary name is required")
    @Size(min = 2, max = 100, message = "Beneficiary name must be between 2 and 100 characters")
    private String beneficiaryName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @DecimalMax(value = "10000.00", message = "Amount exceeds single transfer limit")
    private BigDecimal amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}
