package com.bankingsystem.service;

import com.bankingsystem.dto.notification.*;
import com.bankingsystem.entity.Notification;
import com.bankingsystem.entity.User;
import com.bankingsystem.enums.NotificationPriority;
import com.bankingsystem.enums.NotificationStatus;
import com.bankingsystem.enums.NotificationType;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.repository.NotificationRepository;
import com.bankingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing notifications and alerts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    /**
     * Send notification to a user.
     */
    @Transactional
    public NotificationResponse sendNotification(Long userId, SendNotificationRequest request) {
        log.info("Sending notification to user {}: {}", userId, request.getTitle());

        User user = getUserById(userId);

        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .priority(request.getPriority())
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        auditService.logSystemAction("NOTIFICATION_SENT", 
                "Notification sent to user " + userId + ": " + request.getTitle(), "NOTIFICATION", "SYSTEM");

        log.info("Notification sent successfully with ID: {}", savedNotification.getId());

        return mapToNotificationResponse(savedNotification);
    }

    /**
     * Send bulk notifications to multiple users.
     */
    @Transactional
    public List<NotificationResponse> sendBulkNotifications(BulkNotificationRequest request) {
        log.info("Sending bulk notification to {} users: {}", request.getUserIds().size(), request.getTitle());

        List<NotificationResponse> responses = request.getUserIds().stream()
                .map(userId -> {
                    try {
                        SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                                .title(request.getTitle())
                                .message(request.getMessage())
                                .type(request.getType())
                                .priority(request.getPriority())
                                .build();
                        return sendNotification(userId, notificationRequest);
                    } catch (Exception e) {
                        log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
                        return null;
                    }
                })
                .filter(response -> response != null)
                .toList();

        log.info("Bulk notification sent to {}/{} users successfully", 
                responses.size(), request.getUserIds().size());

        return responses;
    }

    /**
     * Send transaction alert notification.
     */
    @Transactional
    public NotificationResponse sendTransactionAlert(Long userId, TransactionAlertRequest request) {
        log.info("Sending transaction alert to user {}: {}", userId, request.getTransactionType());

        User user = getUserById(userId);

        String title = "Transaction Alert";
        String message = String.format("A %s transaction of %s %s has been processed on your account %s at %s",
                request.getTransactionType(),
                request.getCurrency(),
                request.getAmount(),
                request.getAccountNumber(),
                request.getTransactionTime().toString());

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(NotificationType.TRANSACTION_ALERT)
                .priority(request.getPriority())
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .relatedEntityId(request.getTransactionId())
                .relatedEntityType("TRANSACTION")
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        auditService.logUserAction(userId, "TRANSACTION_ALERT_SENT", 
                "Transaction alert sent for " + request.getTransactionType() + " of " + request.getAmount(), "NOTIFICATION");

        return mapToNotificationResponse(savedNotification);
    }

    /**
     * Send security alert notification.
     */
    @Transactional
    public NotificationResponse sendSecurityAlert(Long userId, SecurityAlertRequest request) {
        log.info("Sending security alert to user {}: {}", userId, request.getEventType());

        User user = getUserById(userId);

        String title = "Security Alert";
        String message = String.format("Security Event: %s detected at %s. Location: %s. If this wasn't you, please contact us immediately.",
                request.getEventType(),
                request.getEventTime().toString(),
                request.getLocation() != null ? request.getLocation() : "Unknown");

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(NotificationType.SECURITY_ALERT)
                .priority(NotificationPriority.HIGH)
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .relatedEntityId(request.getEventId())
                .relatedEntityType("SECURITY_EVENT")
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        auditService.logSecurityEvent("SECURITY_ALERT_SENT", 
                "Security alert sent for " + request.getEventType(), "NOTIFICATION", "HIGH", userId);

        return mapToNotificationResponse(savedNotification);
    }

    /**
     * Get user notifications with pagination and filtering.
     */
    public List<NotificationResponse> getUserNotifications(Long userId, NotificationFilterRequest filter) {
        User user = getUserById(userId);

        List<Notification> notifications;

        if (filter.getType() != null && filter.getStatus() != null) {
            notifications = notificationRepository.findByUserAndTypeAndStatusOrderByCreatedAtDesc(
                    user, filter.getType(), filter.getStatus());
        } else if (filter.getType() != null) {
            notifications = notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(
                    user, filter.getType());
        } else if (filter.getStatus() != null) {
            notifications = notificationRepository.findByUserAndStatusOrderByCreatedAtDesc(
                    user, filter.getStatus());
        } else {
            notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        }

        return notifications.stream()
                .map(this::mapToNotificationResponse)
                .toList();
    }

    /**
     * Mark notification as read.
     */
    @Transactional
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = getNotificationById(notificationId);

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found for user");
        }

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);

        log.info("Notification {} marked as read by user {}", notificationId, userId);

        return mapToNotificationResponse(savedNotification);
    }

    /**
     * Mark all notifications as read for a user.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = getUserById(userId);

        List<Notification> unreadNotifications = notificationRepository.findByUserAndStatus(
                user, NotificationStatus.SENT);

        unreadNotifications.forEach(notification -> {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
        });

        notificationRepository.saveAll(unreadNotifications);

        log.info("All notifications marked as read for user {}", userId);
    }

    /**
     * Delete notification.
     */
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = getNotificationById(notificationId);

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found for user");
        }

        notificationRepository.delete(notification);

        log.info("Notification {} deleted by user {}", notificationId, userId);
    }

    /**
     * Get notification statistics for a user.
     */
    public NotificationStatsResponse getNotificationStats(Long userId) {
        User user = getUserById(userId);

        long totalCount = notificationRepository.countByUser(user);
        long unreadCount = notificationRepository.countByUserAndStatus(user, NotificationStatus.SENT);
        long readCount = notificationRepository.countByUserAndStatus(user, NotificationStatus.READ);

        return NotificationStatsResponse.builder()
                .totalCount(totalCount)
                .unreadCount(unreadCount)
                .readCount(readCount)
                .build();
    }

    /**
     * Get notification preferences for a user.
     */
    public NotificationPreferencesResponse getNotificationPreferences(Long userId) {
        User user = getUserById(userId);

        // Default preferences - in a real system, these would be stored in a preferences table
        return NotificationPreferencesResponse.builder()
                .emailNotifications(true)
                .smsNotifications(false)
                .pushNotifications(true)
                .transactionAlerts(true)
                .securityAlerts(true)
                .marketingNotifications(false)
                .build();
    }

    /**
     * Update notification preferences for a user.
     */
    @Transactional
    public NotificationPreferencesResponse updateNotificationPreferences(Long userId, 
                                                                        NotificationPreferencesRequest request) {
        // Validate user exists
        getUserById(userId);

        // In a real system, these preferences would be saved to a preferences table
        auditService.logUserAction(userId, "NOTIFICATION_PREFERENCES_UPDATED", 
                "Notification preferences updated", "NOTIFICATION");

        log.info("Notification preferences updated for user {}", userId);

        return NotificationPreferencesResponse.builder()
                .emailNotifications(request.isEmailNotifications())
                .smsNotifications(request.isSmsNotifications())
                .pushNotifications(request.isPushNotifications())
                .transactionAlerts(request.isTransactionAlerts())
                .securityAlerts(request.isSecurityAlerts())
                .marketingNotifications(request.isMarketingNotifications())
                .build();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .priority(notification.getPriority())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .build();
    }
}
