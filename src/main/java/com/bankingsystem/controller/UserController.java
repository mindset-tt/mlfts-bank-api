package com.bankingsystem.controller;

import com.bankingsystem.dto.request.PasswordResetRequest;
import com.bankingsystem.dto.response.UserResponse;
import com.bankingsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for user management operations.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users and customer service operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve user details by username")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<UserResponse> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve user details by email")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(description = "Email") @PathVariable String email) {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/customers")
    @Operation(summary = "Get all customers", description = "Retrieve all customer accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<List<UserResponse>> getAllCustomers() {
        List<UserResponse> customers = userService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "Reset user password", description = "Reset password for a customer (Teller/Admin function)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<UserResponse> resetUserPassword(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody PasswordResetRequest request) {
        UserResponse user = userService.resetUserPassword(id, request.getNewPassword());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "Enable user account", description = "Enable a user account")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<UserResponse> enableUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserResponse user = userService.enableUser(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "Disable user account", description = "Disable a user account")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<UserResponse> disableUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserResponse user = userService.disableUser(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/unlock")
    @Operation(summary = "Unlock user account", description = "Unlock a locked user account")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<UserResponse> unlockUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserResponse user = userService.unlockUser(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by name, username, or email")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TELLER')")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @Parameter(description = "Search query") @RequestParam String query) {
        List<UserResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }
}
