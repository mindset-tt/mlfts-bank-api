package com.bankingsystem.dto.response;

import com.bankingsystem.enums.UserRole;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for user response.
 */
@Data
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isEnabled;
    private Boolean kycVerified;
    private Boolean twoFactorEnabled;
    private UserRole role;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
