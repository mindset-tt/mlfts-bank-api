package com.bankingsystem.entity;

import com.bankingsystem.enums.NotificationPriority;
import com.bankingsystem.enums.NotificationStatus;
import com.bankingsystem.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Notification entity representing user notifications and alerts.
 */
@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @NotNull(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "Message is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    @Column(nullable = false, length = 1000)
    private String message;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.SENT;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "related_entity_id")
    private String relatedEntityId;

    @Size(max = 50, message = "Related entity type cannot exceed 50 characters")
    @Column(name = "related_entity_type")
    private String relatedEntityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    public boolean isRead() {
        return NotificationStatus.READ.equals(this.status);
    }

    public boolean isUnread() {
        return NotificationStatus.SENT.equals(this.status);
    }

    public boolean isHighPriority() {
        return NotificationPriority.HIGH.equals(this.priority) || 
               NotificationPriority.URGENT.equals(this.priority);
    }
}
