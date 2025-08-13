package com.bankingsystem.controller;

import com.bankingsystem.dto.request.AccountCreationRequest;
import com.bankingsystem.dto.response.AccountResponse;
import com.bankingsystem.service.AccountService;
import com.bankingsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for account management operations.
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new account", description = "Create a new bank account for the authenticated user")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreationRequest request) {
        Long userId = getCurrentUserId();
        AccountResponse response = accountService.createAccount(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get user accounts", description = "Retrieve all accounts for the authenticated user")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<List<AccountResponse>> getUserAccounts() {
        Long userId = getCurrentUserId();
        List<AccountResponse> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID", description = "Retrieve account details by account ID")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<AccountResponse> getAccountById(
            @Parameter(description = "Account ID") @PathVariable Long accountId) {
        AccountResponse account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get account by number", description = "Retrieve account details by account number")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            @Parameter(description = "Account number") @PathVariable String accountNumber) {
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{accountId}/freeze")
    @Operation(summary = "Freeze account", description = "Freeze the specified account")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<AccountResponse> freezeAccount(
            @Parameter(description = "Account ID") @PathVariable Long accountId) {
        AccountResponse account = accountService.freezeAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{accountId}/unfreeze")
    @Operation(summary = "Unfreeze account", description = "Unfreeze the specified account")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<AccountResponse> unfreezeAccount(
            @Parameter(description = "Account ID") @PathVariable Long accountId) {
        AccountResponse account = accountService.unfreezeAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{accountId}")
    @Operation(summary = "Close account", description = "Close the specified account")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<Void> closeAccount(
            @Parameter(description = "Account ID") @PathVariable Long accountId) {
        accountService.closeAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }
}
