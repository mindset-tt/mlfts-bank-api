package com.bankingsystem.controller;

import com.bankingsystem.dto.request.AdminRegistrationRequest;
import com.bankingsystem.dto.request.LoginRequest;
import com.bankingsystem.dto.request.UserRegistrationRequest;
import com.bankingsystem.dto.response.AuthenticationResponse;
import com.bankingsystem.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication operations.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        try {
            AuthenticationResponse response = authenticationService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/register-admin")
    @Operation(summary = "Register a new admin/staff user", 
               description = "Create admin, manager, teller, loan officer, or security officer accounts")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<AuthenticationResponse> registerAdmin(
            @Valid @RequestBody AdminRegistrationRequest request) {
        try {
            AuthenticationResponse response = authenticationService.registerAdmin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Admin registration error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/setup-admin")
    @Operation(summary = "Setup initial admin user", 
               description = "Create the first admin user - only works if no admin exists")
    public ResponseEntity<AuthenticationResponse> setupInitialAdmin(
            @Valid @RequestBody AdminRegistrationRequest request) {
        try {
            // Only allow ADMIN role for initial setup
            request.setRole(com.bankingsystem.enums.UserRole.ADMIN);
            
            AuthenticationResponse response = authenticationService.registerAdmin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Initial admin setup error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request, 
            HttpServletRequest httpRequest) {
        try {
            AuthenticationResponse response = authenticationService.login(request, httpRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @RequestParam String refreshToken) {
        AuthenticationResponse response = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<String> logout(@RequestParam String username) {
        authenticationService.logout(username);
        return ResponseEntity.ok("Logged out successfully");
    }
}
