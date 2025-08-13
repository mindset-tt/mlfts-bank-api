package com.bankingsystem.dto.payment;

import com.bankingsystem.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for bill payment operations.
 */
@Data
@Builder
public class BillPaymentResponse {

    private String paymentReference;
    private String billerCode;
    private String billerName;
    private String customerReference;
    private BigDecimal amount;
    private BigDecimal fee;
    private PaymentStatus status;
    private LocalDateTime processedAt;
}
