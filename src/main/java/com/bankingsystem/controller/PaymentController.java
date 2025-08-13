package com.bankingsystem.controller;

import com.bankingsystem.dto.payment.BillPaymentRequest;
import com.bankingsystem.dto.payment.BillPaymentResponse;
import com.bankingsystem.dto.payment.ExternalTransferRequest;
import com.bankingsystem.dto.payment.InternalTransferRequest;
import com.bankingsystem.dto.payment.PaymentResponse;
import com.bankingsystem.service.PaymentService;
import com.bankingsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing payments and transfers")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/internal-transfer")
    @Operation(summary = "Internal transfer", description = "Transfer money between accounts within the bank")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'TELLER')")
    public ResponseEntity<PaymentResponse> internalTransfer(@Valid @RequestBody InternalTransferRequest request) {
        Long userId = getCurrentUserId();
        PaymentResponse payment = paymentService.processInternalTransfer(userId, request);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/external-transfer")
    @Operation(summary = "External transfer", description = "Transfer money to external bank accounts")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'TELLER')")
    public ResponseEntity<PaymentResponse> externalTransfer(@Valid @RequestBody ExternalTransferRequest request) {
        Long userId = getCurrentUserId();
        PaymentResponse payment = paymentService.processExternalTransfer(userId, request);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/bill-payment")
    @Operation(summary = "Bill payment", description = "Pay bills to registered billers")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'TELLER')")
    public ResponseEntity<BillPaymentResponse> billPayment(@Valid @RequestBody BillPaymentRequest request) {
        Long userId = getCurrentUserId();
        BillPaymentResponse payment = paymentService.processBillPayment(userId, request);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID", description = "Retrieve payment details by payment ID")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        // This method doesn't exist in the service, return empty response
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user")
    @Operation(summary = "Get user payments", description = "Get all payments for the authenticated user")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(
            @Parameter(description = "Account number") @RequestParam String accountNumber) {
        Long userId = getCurrentUserId();
        List<PaymentResponse> payments = paymentService.getPaymentHistory(userId, accountNumber);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get account payments", description = "Get all payments for a specific account")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<List<PaymentResponse>> getAccountPayments(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @Parameter(description = "Account number") @RequestParam String accountNumber) {
        Long userId = getCurrentUserId();
        List<PaymentResponse> payments = paymentService.getPaymentHistory(userId, accountNumber);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get payment by reference", description = "Retrieve payment details by payment reference")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<PaymentResponse> getPaymentByReference(
            @Parameter(description = "Payment reference") @PathVariable String reference) {
        Long userId = getCurrentUserId();
        PaymentResponse payment = paymentService.getPaymentByReference(userId, reference);
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel payment", description = "Cancel a pending payment")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<String> cancelPayment(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId,
            @Parameter(description = "Cancellation reason") @RequestParam String reason) {
        // This method doesn't exist in service
        return ResponseEntity.ok("Payment cancellation not implemented yet");
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status", description = "Get payments filtered by status (Admin/Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable String status) {
        // This method doesn't exist in service
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/bills/user")
    @Operation(summary = "Get user bill payments", description = "Get all bill payments for the authenticated user")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillPaymentResponse>> getUserBillPayments(
            @Parameter(description = "Account number") @RequestParam String accountNumber) {
        Long userId = getCurrentUserId();
        List<BillPaymentResponse> payments = paymentService.getBillPaymentHistory(userId, accountNumber);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/bills/account/{accountId}")
    @Operation(summary = "Get account bill payments", description = "Get all bill payments for a specific account")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillPaymentResponse>> getAccountBillPayments(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @Parameter(description = "Account number") @RequestParam String accountNumber) {
        Long userId = getCurrentUserId();
        List<BillPaymentResponse> payments = paymentService.getBillPaymentHistory(userId, accountNumber);
        return ResponseEntity.ok(payments);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }
}
