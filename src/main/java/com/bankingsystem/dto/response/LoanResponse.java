package com.bankingsystem.dto.response;

import com.bankingsystem.enums.LoanStatus;
import com.bankingsystem.enums.LoanType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for loan responses.
 */
@Data
public class LoanResponse {
    
    private Long id;
    private String loanNumber;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termInMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal outstandingBalance;
    private BigDecimal totalAmount;
    private BigDecimal totalInterest;
    private LoanStatus status;
    private LocalDateTime applicationDate;
    private LocalDateTime approvalDate;
    private LocalDateTime disbursementDate;
    private LocalDate nextPaymentDate;
    private LocalDate maturityDate;
    private String purpose;
    private Boolean isSecured;
}
