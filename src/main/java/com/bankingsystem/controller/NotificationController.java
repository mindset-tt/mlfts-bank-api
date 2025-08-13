package com.bankingsystem.controller;

import com.bankingsystem.dto.notification.TransactionAlertRequest;
import com.bankingsystem.dto.notification.SendNotificationRequest;
import com.bankingsystem.dto.notification.NotificationFilterRequest;
import com.bankingsystem.dto.notification.NotificationResponse;
import com.bankingsystem.service.NotificationService;
import com.bankingsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for managing notifications and alerts")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @PostMapping("/transaction-alert")
    @Operation(summary = "Send transaction alert", description = "Send a transaction alert notification")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<NotificationResponse> sendTransactionAlert(@Valid @RequestBody TransactionAlertRequest request) {
        Long userId = getCurrentUserId();
        NotificationResponse notification = notificationService.sendTransactionAlert(userId, request);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/send")
    @Operation(summary = "Send notification", description = "Send a notification to a user")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse notification = notificationService.sendNotification(userId, request);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user")
    @Operation(summary = "Get user notifications", description = "Get all notifications for the authenticated user")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @Valid @RequestBody(required = false) NotificationFilterRequest filter) {
        Long userId = getCurrentUserId();
        if (filter == null) {
            filter = new NotificationFilterRequest(); // Create default filter
        }
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId, filter);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark as read", description = "Mark a notification as read")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<NotificationResponse> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        NotificationResponse notification = notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/user/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for the user")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<String> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification", description = "Delete a notification")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<String> deleteNotification(
            @Parameter(description = "Notification ID") @PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok("Notification deleted successfully");
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }
}
