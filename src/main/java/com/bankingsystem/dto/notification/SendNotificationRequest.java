package com.bankingsystem.dto.notification;

import com.bankingsystem.enums.NotificationPriority;
import com.bankingsystem.enums.NotificationType;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for sending notifications.
 */
@Data
@Builder
public class SendNotificationRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Priority is required")
    private NotificationPriority priority;
}
