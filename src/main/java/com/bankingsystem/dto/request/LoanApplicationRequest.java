package com.bankingsystem.dto.request;

import com.bankingsystem.enums.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for loan application requests.
 */
@Data
public class LoanApplicationRequest {
    
    @NotNull(message = "Loan type is required")
    private LoanType loanType;
    
    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is $1,000")
    private BigDecimal principalAmount;
    
    @NotNull(message = "Term in months is required")
    @Min(value = 12, message = "Minimum loan term is 12 months")
    @Max(value = 360, message = "Maximum loan term is 360 months")
    private Integer termInMonths;
    
    @NotNull(message = "Annual income is required")
    @DecimalMin(value = "0.00", message = "Annual income must be positive")
    private BigDecimal annualIncome;
    
    @Min(value = 300, message = "Minimum credit score is 300")
    @Max(value = 850, message = "Maximum credit score is 850")
    private Integer creditScore;
    
    private String purpose;
    
    private Boolean isSecured = false;
    
    private String collateralDescription;
    
    private BigDecimal collateralValue;
}
