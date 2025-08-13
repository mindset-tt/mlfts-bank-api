package com.bankingsystem.dto.notification;

import com.bankingsystem.enums.NotificationPriority;
import com.bankingsystem.enums.NotificationStatus;
import com.bankingsystem.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for notification operations.
 */
@Data
@Builder
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationPriority priority;
    private NotificationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String relatedEntityId;
    private String relatedEntityType;
}
