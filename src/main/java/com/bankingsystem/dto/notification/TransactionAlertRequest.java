package com.bankingsystem.dto.notification;

import com.bankingsystem.enums.NotificationPriority;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for transaction alert notifications.
 */
@Data
public class TransactionAlertRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Transaction type is required")
    private String transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotNull(message = "Transaction time is required")
    private LocalDateTime transactionTime;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotNull(message = "Priority is required")
    private NotificationPriority priority;
}
