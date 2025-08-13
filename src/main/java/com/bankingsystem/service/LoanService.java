package com.bankingsystem.service;

import com.bankingsystem.dto.request.LoanApplicationRequest;
import com.bankingsystem.dto.request.LoanPaymentRequest;
import com.bankingsystem.dto.response.LoanResponse;
import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.Loan;
import com.bankingsystem.entity.LoanPayment;
import com.bankingsystem.entity.User;
import com.bankingsystem.enums.LoanStatus;
import com.bankingsystem.enums.LoanType;
import com.bankingsystem.enums.PaymentStatus;
import com.bankingsystem.enums.TransactionStatus;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.exception.InvalidOperationException;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.LoanRepository;
import com.bankingsystem.repository.LoanPaymentRepository;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.util.NumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for loan-related operations.
 */
@Service
@Transactional
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanPaymentRepository loanPaymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuditService auditService;

    public LoanResponse applyForLoan(Long userId, LoanApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        // Create new loan application
        Loan loan = new Loan();
        loan.setLoanNumber(NumberGenerator.generateLoanNumber());
        loan.setUser(user);
        loan.setLoanType(request.getLoanType());
        loan.setPrincipalAmount(request.getPrincipalAmount());
        loan.setInterestRate(calculateInterestRate(request.getLoanType(), request.getCreditScore()));
        loan.setTermInMonths(request.getTermInMonths());
        loan.setPurpose(request.getPurpose());
        loan.setAnnualIncome(request.getAnnualIncome());
        loan.setCreditScore(request.getCreditScore());
        loan.setIsSecured(request.getIsSecured());
        loan.setCollateralDescription(request.getCollateralDescription());
        loan.setCollateralValue(request.getCollateralValue());
        loan.setStatus(LoanStatus.APPLIED);
        loan.setApplicationDate(LocalDateTime.now());
        
        // Calculate debt-to-income ratio
        BigDecimal monthlyIncome = request.getAnnualIncome().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = calculateMonthlyPayment(
            request.getPrincipalAmount(), 
            loan.getInterestRate(), 
            request.getTermInMonths()
        );
        loan.setMonthlyPayment(monthlyPayment);
        loan.setDebtToIncomeRatio(monthlyPayment.divide(monthlyIncome, 4, RoundingMode.HALF_UP));

        // Calculate total amounts
        BigDecimal totalAmount = monthlyPayment.multiply(BigDecimal.valueOf(request.getTermInMonths()));
        loan.setTotalAmount(totalAmount);
        loan.setTotalInterest(totalAmount.subtract(request.getPrincipalAmount()));
        loan.setOutstandingBalance(request.getPrincipalAmount());

        Loan savedLoan = loanRepository.save(loan);

        auditService.logUserAction(userId, "LOAN_APPLICATION", 
            "Applied for " + request.getLoanType() + " loan: " + savedLoan.getLoanNumber(), "LOAN");

        return convertToResponse(savedLoan);
    }

    public LoanResponse approveLoan(Long loanId, Long disbursementAccountId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId.toString()));

        if (loan.getStatus() != LoanStatus.APPLIED && loan.getStatus() != LoanStatus.UNDER_REVIEW) {
            throw new InvalidOperationException("Loan cannot be approved in current status: " + loan.getStatus());
        }

        Account disbursementAccount = accountRepository.findById(disbursementAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", disbursementAccountId.toString()));

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDateTime.now());
        loan.setDisbursementAccount(disbursementAccount);
        loan.setDisbursementDate(LocalDateTime.now());
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setMaturityDate(LocalDate.now().plusMonths(loan.getTermInMonths()));

        // Disburse funds to account
        disbursementAccount.setBalance(disbursementAccount.getBalance().add(loan.getPrincipalAmount()));
        disbursementAccount.setAvailableBalance(disbursementAccount.getAvailableBalance().add(loan.getPrincipalAmount()));
        accountRepository.save(disbursementAccount);

        loan.setStatus(LoanStatus.ACTIVE);
        Loan savedLoan = loanRepository.save(loan);

        auditService.logUserAction(loan.getUser().getId(), "LOAN_APPROVED", 
            "Loan approved and disbursed: " + loan.getLoanNumber(), "LOAN");

        return convertToResponse(savedLoan);
    }

    public LoanResponse rejectLoan(Long loanId, String reason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId.toString()));

        if (loan.getStatus() != LoanStatus.APPLIED && loan.getStatus() != LoanStatus.UNDER_REVIEW) {
            throw new InvalidOperationException("Loan cannot be rejected in current status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setNotes(reason);
        Loan savedLoan = loanRepository.save(loan);

        auditService.logUserAction(loan.getUser().getId(), "LOAN_REJECTED", 
            "Loan rejected: " + loan.getLoanNumber() + " - " + reason, "LOAN");

        return convertToResponse(savedLoan);
    }

    public LoanResponse makeLoanPayment(Long loanId, LoanPaymentRequest request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId.toString()));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new InvalidOperationException("Cannot make payment for inactive loan");
        }

        Account paymentAccount = accountRepository.findById(request.getPaymentAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.getPaymentAccountId().toString()));

        if (paymentAccount.getAvailableBalance().compareTo(request.getPaymentAmount()) < 0) {
            throw new InvalidOperationException("Insufficient funds for loan payment");
        }

        // Create loan payment record
        LoanPayment payment = new LoanPayment();
        payment.setLoan(loan);
        payment.setPaymentAccount(paymentAccount);
        payment.setPaymentAmount(request.getPaymentAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setDueDate(loan.getNextPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(TransactionStatus.COMPLETED);
        payment.setPaymentReference(NumberGenerator.generateTransactionReference());
        payment.setIsAutoPayment(request.getIsAutoPayment());

        // Calculate interest and principal portions
        BigDecimal monthlyInterestRate = loan.getInterestRate().divide(BigDecimal.valueOf(1200), 6, RoundingMode.HALF_UP);
        BigDecimal interestAmount = loan.getOutstandingBalance().multiply(monthlyInterestRate);
        BigDecimal principalAmount = request.getPaymentAmount().subtract(interestAmount);

        if (principalAmount.compareTo(BigDecimal.ZERO) < 0) {
            principalAmount = BigDecimal.ZERO;
            interestAmount = request.getPaymentAmount();
        }

        payment.setInterestAmount(interestAmount);
        payment.setPrincipalAmount(principalAmount);
        payment.setRemainingBalance(loan.getOutstandingBalance().subtract(principalAmount));

        // Update account balance
        paymentAccount.setBalance(paymentAccount.getBalance().subtract(request.getPaymentAmount()));
        paymentAccount.setAvailableBalance(paymentAccount.getAvailableBalance().subtract(request.getPaymentAmount()));
        accountRepository.save(paymentAccount);

        // Update loan
        loan.setOutstandingBalance(payment.getRemainingBalance());
        loan.setNextPaymentDate(loan.getNextPaymentDate().plusMonths(1));

        // Check if loan is paid off
        if (loan.getOutstandingBalance().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            loan.setStatus(LoanStatus.PAID_OFF);
            loan.setOutstandingBalance(BigDecimal.ZERO);
        }

        loanPaymentRepository.save(payment);
        Loan savedLoan = loanRepository.save(loan);

        auditService.logUserAction(loan.getUser().getId(), "LOAN_PAYMENT", 
            "Payment made for loan: " + loan.getLoanNumber() + " - Amount: " + request.getPaymentAmount(), "LOAN");

        return convertToResponse(savedLoan);
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId.toString()));
        return convertToResponse(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getUserLoans(Long userId) {
        List<Loan> loans = loanRepository.findByUserIdOrderByApplicationDateDesc(userId);
        return loans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByStatus(LoanStatus status) {
        List<Loan> loans = loanRepository.findByStatusOrderByApplicationDateDesc(status);
        return loans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateInterestRate(LoanType loanType, Integer creditScore) {
        BigDecimal baseRate;
        
        switch (loanType) {
            case PERSONAL:
                baseRate = BigDecimal.valueOf(8.50);
                break;
            case HOME:
                baseRate = BigDecimal.valueOf(3.50);
                break;
            case AUTO:
                baseRate = BigDecimal.valueOf(5.00);
                break;
            case BUSINESS:
                baseRate = BigDecimal.valueOf(7.00);
                break;
            case EDUCATION:
                baseRate = BigDecimal.valueOf(4.50);
                break;
            case CREDIT_LINE:
                baseRate = BigDecimal.valueOf(9.00);
                break;
            default:
                baseRate = BigDecimal.valueOf(8.00);
        }

        // Adjust based on credit score
        if (creditScore >= 750) {
            baseRate = baseRate.subtract(BigDecimal.valueOf(1.00));
        } else if (creditScore >= 700) {
            baseRate = baseRate.subtract(BigDecimal.valueOf(0.50));
        } else if (creditScore < 600) {
            baseRate = baseRate.add(BigDecimal.valueOf(2.00));
        } else if (creditScore < 650) {
            baseRate = baseRate.add(BigDecimal.valueOf(1.00));
        }

        return baseRate;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, Integer months) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 6, RoundingMode.HALF_UP);
        
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRToN = onePlusR.pow(months);
        
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private LoanResponse convertToResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setLoanNumber(loan.getLoanNumber());
        response.setLoanType(loan.getLoanType());
        response.setPrincipalAmount(loan.getPrincipalAmount());
        response.setInterestRate(loan.getInterestRate());
        response.setTermInMonths(loan.getTermInMonths());
        response.setMonthlyPayment(loan.getMonthlyPayment());
        response.setOutstandingBalance(loan.getOutstandingBalance());
        response.setTotalAmount(loan.getTotalAmount());
        response.setTotalInterest(loan.getTotalInterest());
        response.setStatus(loan.getStatus());
        response.setApplicationDate(loan.getApplicationDate());
        response.setApprovalDate(loan.getApprovalDate());
        response.setDisbursementDate(loan.getDisbursementDate());
        response.setNextPaymentDate(loan.getNextPaymentDate());
        response.setMaturityDate(loan.getMaturityDate());
        response.setPurpose(loan.getPurpose());
        response.setIsSecured(loan.getIsSecured());
        return response;
    }
}
