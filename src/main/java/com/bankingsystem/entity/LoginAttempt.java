package com.bankingsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LoginAttempt entity for tracking login attempts and security.
 */
@Entity
@Table(name = "login_attempts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttempt extends BaseEntity {

    @NotNull(message = "Username is required")
    @Size(max = 50, message = "Username cannot exceed 50 characters")
    @Column(nullable = false)
    private String username;

    @NotNull(message = "IP address is required")
    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @NotNull(message = "User agent is required")
    @Size(max = 500, message = "User agent cannot exceed 500 characters")
    @Column(name = "user_agent", nullable = false)
    private String userAgent;

    @Column(name = "attempt_time", nullable = false)
    private LocalDateTime attemptTime;

    @Column(name = "is_successful", nullable = false)
    private Boolean isSuccessful;

    @Size(max = 500, message = "Failure reason cannot exceed 500 characters")
    @Column(name = "failure_reason")
    private String failureReason;

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    @Size(max = 100, message = "Device cannot exceed 100 characters")
    private String device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        if (attemptTime == null) {
            attemptTime = LocalDateTime.now();
        }
    }
}
