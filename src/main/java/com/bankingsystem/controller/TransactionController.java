package com.bankingsystem.controller;

import com.bankingsystem.dto.request.TransferRequest;
import com.bankingsystem.dto.response.TransactionResponse;
import com.bankingsystem.enums.TransactionType;
import com.bankingsystem.service.TransactionService;
import com.bankingsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing banking transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money", description = "Deposit money to an account")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'TELLER')")
    public ResponseEntity<TransactionResponse> deposit(
            @Parameter(description = "Account ID") @RequestParam Long accountId,
            @Parameter(description = "Amount to deposit") @RequestParam BigDecimal amount,
            @Parameter(description = "Description") @RequestParam(required = false) String description) {
        TransactionResponse transaction = transactionService.deposit(accountId, amount, description);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraw money from an account")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'TELLER')")
    public ResponseEntity<TransactionResponse> withdraw(
            @Parameter(description = "Account ID") @RequestParam Long accountId,
            @Parameter(description = "Amount to withdraw") @RequestParam BigDecimal amount,
            @Parameter(description = "Description") @RequestParam(required = false) String description) {
        TransactionResponse transaction = transactionService.withdraw(accountId, amount, description);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfer money between accounts")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'TELLER')")
    public ResponseEntity<List<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        List<TransactionResponse> transactions = transactionService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get account transactions", description = "Get all transactions for a specific account")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<Page<TransactionResponse>> getAccountTransactions(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(accountId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user")
    @Operation(summary = "Get user transactions", description = "Get all transactions for the authenticated user")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<Page<TransactionResponse>> getUserTransactions(Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<TransactionResponse> transactions = transactionService.getTransactionsByUserId(userId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction by ID", description = "Get transaction details by transaction ID")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "Transaction ID") @PathVariable Long transactionId) {
        TransactionResponse transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get transactions by type", description = "Get transactions filtered by type")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByType(
            @Parameter(description = "Transaction type") @PathVariable TransactionType type,
            Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getTransactionsByType(type, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountId}/date-range")
    @Operation(summary = "Get account transactions by date range", description = "Get account transactions within a date range")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactionsByDateRange(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam LocalDateTime endDate) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByDateRange(accountId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }
}
