package com.bankingsystem.dto.payment;

import com.bankingsystem.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment operations.
 */
@Data
@Builder
public class PaymentResponse {

    private String paymentReference;
    private BigDecimal amount;
    private BigDecimal fee;
    private PaymentStatus status;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String description;
    private LocalDateTime processedAt;
}
