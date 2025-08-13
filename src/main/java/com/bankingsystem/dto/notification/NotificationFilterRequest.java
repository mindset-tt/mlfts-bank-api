package com.bankingsystem.dto.notification;

import com.bankingsystem.enums.NotificationStatus;
import com.bankingsystem.enums.NotificationType;
import lombok.Data;

/**
 * Request DTO for filtering notifications.
 */
@Data
public class NotificationFilterRequest {

    private NotificationType type;
    private NotificationStatus status;
    private Integer page = 0;
    private Integer size = 20;
}
