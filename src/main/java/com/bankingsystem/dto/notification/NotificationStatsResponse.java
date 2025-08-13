package com.bankingsystem.dto.notification;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for notification statistics.
 */
@Data
@Builder
public class NotificationStatsResponse {

    private long totalCount;
    private long unreadCount;
    private long readCount;
}
