package com.bankingsystem.dto.notification;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request DTO for security alert notifications.
 */
@Data
public class SecurityAlertRequest {

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotNull(message = "Event time is required")
    private LocalDateTime eventTime;

    @NotBlank(message = "Event ID is required")
    private String eventId;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @Size(max = 500, message = "Details cannot exceed 500 characters")
    private String details;
}
