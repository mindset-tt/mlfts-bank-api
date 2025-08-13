package com.bankingsystem.service;

import com.bankingsystem.dto.payment.*;
import com.bankingsystem.entity.*;
import com.bankingsystem.enums.PaymentStatus;
import com.bankingsystem.enums.PaymentType;
import com.bankingsystem.exception.InsufficientFundsException;
import com.bankingsystem.exception.InvalidOperationException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.repository.*;
import com.bankingsystem.util.NumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing payments, transfers, and bill payments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final AuditService auditService;

    private static final BigDecimal TRANSFER_FEE = new BigDecimal("2.50");
    private static final BigDecimal BILL_PAYMENT_FEE = new BigDecimal("1.00");
    private static final BigDecimal MAX_DAILY_TRANSFER_LIMIT = new BigDecimal("50000.00");
    private static final BigDecimal MAX_SINGLE_TRANSFER_LIMIT = new BigDecimal("10000.00");

    /**
     * Process internal bank transfer between accounts.
     */
    @Transactional
    public PaymentResponse processInternalTransfer(Long userId, InternalTransferRequest request) {
        log.info("Processing internal transfer from account {} to account {}", 
                request.getFromAccountNumber(), request.getToAccountNumber());

        // Validate accounts
        Account fromAccount = getAccountByNumber(request.getFromAccountNumber());
        Account toAccount = getAccountByNumber(request.getToAccountNumber());

        validateAccountForTransfer(fromAccount, userId);
        validateTransferAmount(request.getAmount());
        validateDailyTransferLimit(fromAccount, request.getAmount());

        // Check sufficient funds (including fees)
        BigDecimal totalAmount = request.getAmount().add(TRANSFER_FEE);
        if (fromAccount.getBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer including fees");
        }

        // Create payment record
        Payment payment = Payment.builder()
                .paymentReference(NumberGenerator.generatePaymentReference())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .fee(TRANSFER_FEE)
                .paymentType(PaymentType.INTERNAL_TRANSFER)
                .status(PaymentStatus.PENDING)
                .description(request.getDescription())
                .scheduledDate(LocalDateTime.now())
                .build();

        // Process transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(totalAmount));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProcessedAt(LocalDateTime.now());

        // Save entities
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        Payment savedPayment = paymentRepository.save(payment);

        // Log transaction
        auditService.logUserAction(userId, "INTERNAL_TRANSFER", 
                "Transfer of " + request.getAmount() + " from " + request.getFromAccountNumber() + 
                " to " + request.getToAccountNumber(), "PAYMENT");

        log.info("Internal transfer completed successfully with reference: {}", 
                savedPayment.getPaymentReference());

        return PaymentResponse.builder()
                .paymentReference(savedPayment.getPaymentReference())
                .amount(savedPayment.getAmount())
                .fee(savedPayment.getFee())
                .status(savedPayment.getStatus())
                .fromAccountNumber(savedPayment.getFromAccount().getAccountNumber())
                .toAccountNumber(savedPayment.getToAccount().getAccountNumber())
                .description(savedPayment.getDescription())
                .processedAt(savedPayment.getProcessedAt())
                .build();
    }

    /**
     * Process external bank transfer.
     */
    @Transactional
    public PaymentResponse processExternalTransfer(Long userId, ExternalTransferRequest request) {
        log.info("Processing external transfer from account {} to external account {}", 
                request.getFromAccountNumber(), request.getToAccountNumber());

        Account fromAccount = getAccountByNumber(request.getFromAccountNumber());
        validateAccountForTransfer(fromAccount, userId);
        validateTransferAmount(request.getAmount());

        // Higher fees for external transfers
        BigDecimal externalTransferFee = TRANSFER_FEE.multiply(new BigDecimal("2"));
        BigDecimal totalAmount = request.getAmount().add(externalTransferFee);

        if (fromAccount.getBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for external transfer including fees");
        }

        // Create payment record for external transfer
        Payment payment = Payment.builder()
                .paymentReference(NumberGenerator.generatePaymentReference())
                .fromAccount(fromAccount)
                .amount(request.getAmount())
                .fee(externalTransferFee)
                .paymentType(PaymentType.EXTERNAL_TRANSFER)
                .status(PaymentStatus.PENDING)
                .description(request.getDescription())
                .externalBankCode(request.getBankCode())
                .externalAccountNumber(request.getToAccountNumber())
                .beneficiaryName(request.getBeneficiaryName())
                .scheduledDate(LocalDateTime.now())
                .build();

        // Deduct amount from source account
        fromAccount.setBalance(fromAccount.getBalance().subtract(totalAmount));
        accountRepository.save(fromAccount);

        // Mark as processing (external transfers take time)
        payment.setStatus(PaymentStatus.PROCESSING);
        Payment savedPayment = paymentRepository.save(payment);

        auditService.logUserAction(userId, "EXTERNAL_TRANSFER", 
                "External transfer of " + request.getAmount() + " to " + request.getBankCode() + 
                " - " + request.getToAccountNumber(), "PAYMENT");

        log.info("External transfer initiated with reference: {}", savedPayment.getPaymentReference());

        return PaymentResponse.builder()
                .paymentReference(savedPayment.getPaymentReference())
                .amount(savedPayment.getAmount())
                .fee(savedPayment.getFee())
                .status(savedPayment.getStatus())
                .fromAccountNumber(savedPayment.getFromAccount().getAccountNumber())
                .toAccountNumber(savedPayment.getExternalAccountNumber())
                .description(savedPayment.getDescription())
                .processedAt(savedPayment.getProcessedAt())
                .build();
    }

    /**
     * Process bill payment.
     */
    @Transactional
    public BillPaymentResponse processBillPayment(Long userId, BillPaymentRequest request) {
        log.info("Processing bill payment from account {} for biller {}", 
                request.getAccountNumber(), request.getBillerCode());

        Account account = getAccountByNumber(request.getAccountNumber());
        validateAccountForTransfer(account, userId);

        BigDecimal totalAmount = request.getAmount().add(BILL_PAYMENT_FEE);
        if (account.getBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for bill payment including fees");
        }

        // Create bill payment record
        BillPayment billPayment = BillPayment.builder()
                .paymentReference(NumberGenerator.generatePaymentReference())
                .account(account)
                .billerCode(request.getBillerCode())
                .billerName(request.getBillerName())
                .customerReference(request.getCustomerReference())
                .amount(request.getAmount())
                .fee(BILL_PAYMENT_FEE)
                .status(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();

        // Process payment
        account.setBalance(account.getBalance().subtract(totalAmount));
        billPayment.setStatus(PaymentStatus.COMPLETED);
        billPayment.setProcessedAt(LocalDateTime.now());

        accountRepository.save(account);
        BillPayment savedBillPayment = billPaymentRepository.save(billPayment);

        auditService.logUserAction(userId, "BILL_PAYMENT", 
                "Bill payment of " + request.getAmount() + " to " + request.getBillerName(), "PAYMENT");

        log.info("Bill payment completed with reference: {}", savedBillPayment.getPaymentReference());

        return BillPaymentResponse.builder()
                .paymentReference(savedBillPayment.getPaymentReference())
                .billerCode(savedBillPayment.getBillerCode())
                .billerName(savedBillPayment.getBillerName())
                .customerReference(savedBillPayment.getCustomerReference())
                .amount(savedBillPayment.getAmount())
                .fee(savedBillPayment.getFee())
                .status(savedBillPayment.getStatus())
                .processedAt(savedBillPayment.getProcessedAt())
                .build();
    }

    /**
     * Get payment history for an account.
     */
    public List<PaymentResponse> getPaymentHistory(Long userId, String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        
        if (!account.getUser().getId().equals(userId)) {
            throw new InvalidOperationException("Access denied to account payment history");
        }

        List<Payment> payments = paymentRepository.findByFromAccountOrToAccountOrderByCreatedAtDesc(account, account);

        return payments.stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }

    /**
     * Get bill payment history for an account.
     */
    public List<BillPaymentResponse> getBillPaymentHistory(Long userId, String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        
        if (!account.getUser().getId().equals(userId)) {
            throw new InvalidOperationException("Access denied to account bill payment history");
        }

        List<BillPayment> billPayments = billPaymentRepository.findByAccountOrderByCreatedAtDesc(account);

        return billPayments.stream()
                .map(this::mapToBillPaymentResponse)
                .toList();
    }

    /**
     * Get payment by reference.
     */
    public PaymentResponse getPaymentByReference(Long userId, String paymentReference) {
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + paymentReference));

        // Verify user has access to this payment
        if (!payment.getFromAccount().getUser().getId().equals(userId) && 
            (payment.getToAccount() == null || !payment.getToAccount().getUser().getId().equals(userId))) {
            throw new InvalidOperationException("Access denied to payment details");
        }

        return mapToPaymentResponse(payment);
    }

    private Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    private void validateAccountForTransfer(Account account, Long userId) {
        if (!account.getUser().getId().equals(userId)) {
            throw new InvalidOperationException("Access denied to account");
        }

        if (!account.getIsActive() || account.getIsFrozen()) {
            throw new InvalidOperationException("Account is not active for transfers");
        }
    }

    private void validateTransferAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Transfer amount must be positive");
        }

        if (amount.compareTo(MAX_SINGLE_TRANSFER_LIMIT) > 0) {
            throw new InvalidOperationException("Transfer amount exceeds single transaction limit");
        }
    }

    private void validateDailyTransferLimit(Account account, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        List<Payment> todaysTransfers = paymentRepository.findByFromAccountAndCreatedAtAfter(account, startOfDay);

        BigDecimal dailyTotal = todaysTransfers.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (dailyTotal.add(amount).compareTo(MAX_DAILY_TRANSFER_LIMIT) > 0) {
            throw new InvalidOperationException("Daily transfer limit exceeded");
        }
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentReference(payment.getPaymentReference())
                .amount(payment.getAmount())
                .fee(payment.getFee())
                .status(payment.getStatus())
                .fromAccountNumber(payment.getFromAccount().getAccountNumber())
                .toAccountNumber(payment.getToAccount() != null ? 
                    payment.getToAccount().getAccountNumber() : payment.getExternalAccountNumber())
                .description(payment.getDescription())
                .processedAt(payment.getProcessedAt())
                .build();
    }

    private BillPaymentResponse mapToBillPaymentResponse(BillPayment billPayment) {
        return BillPaymentResponse.builder()
                .paymentReference(billPayment.getPaymentReference())
                .billerCode(billPayment.getBillerCode())
                .billerName(billPayment.getBillerName())
                .customerReference(billPayment.getCustomerReference())
                .amount(billPayment.getAmount())
                .fee(billPayment.getFee())
                .status(billPayment.getStatus())
                .processedAt(billPayment.getProcessedAt())
                .build();
    }
}
