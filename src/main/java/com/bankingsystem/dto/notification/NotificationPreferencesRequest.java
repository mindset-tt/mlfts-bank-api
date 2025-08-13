package com.bankingsystem.dto.notification;

import lombok.Data;

/**
 * Request DTO for notification preferences.
 */
@Data
public class NotificationPreferencesRequest {

    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean pushNotifications;
    private boolean transactionAlerts;
    private boolean securityAlerts;
    private boolean marketingNotifications;
}
