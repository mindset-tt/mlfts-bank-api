package com.bankingsystem.dto.notification;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for notification preferences.
 */
@Data
@Builder
public class NotificationPreferencesResponse {

    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean pushNotifications;
    private boolean transactionAlerts;
    private boolean securityAlerts;
    private boolean marketingNotifications;
}
