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
 * AuditLog entity for tracking all system activities and changes.
 */
@Entity
@Table(name = "audit_logs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {

    @NotNull(message = "Action is required")
    @Size(max = 100, message = "Action cannot exceed 100 characters")
    @Column(nullable = false)
    private String action;

    @NotNull(message = "Entity type is required")
    @Size(max = 100, message = "Entity type cannot exceed 100 characters")
    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Size(max = 50, message = "Entity ID cannot exceed 50 characters")
    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @NotNull(message = "Timestamp is required")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    @Column(name = "ip_address")
    private String ipAddress;

    @Size(max = 500, message = "User agent cannot exceed 500 characters")
    @Column(name = "user_agent")
    private String userAgent;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 50, message = "Severity cannot exceed 50 characters")
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Size(max = 100, message = "Module cannot exceed 100 characters")
    private String module; // AUTHENTICATION, TRANSACTION, ACCOUNT, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
