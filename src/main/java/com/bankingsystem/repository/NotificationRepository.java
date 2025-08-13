package com.bankingsystem.repository;

import com.bankingsystem.entity.Notification;
import com.bankingsystem.entity.User;
import com.bankingsystem.enums.NotificationStatus;
import com.bankingsystem.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notification entity operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by user ordered by creation date descending.
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find notifications by user and status ordered by creation date descending.
     */
    List<Notification> findByUserAndStatusOrderByCreatedAtDesc(User user, NotificationStatus status);

    /**
     * Find notifications by user and type ordered by creation date descending.
     */
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);

    /**
     * Find notifications by user, type, and status ordered by creation date descending.
     */
    List<Notification> findByUserAndTypeAndStatusOrderByCreatedAtDesc(User user, NotificationType type, NotificationStatus status);

    /**
     * Find notifications by user and status.
     */
    List<Notification> findByUserAndStatus(User user, NotificationStatus status);

    /**
     * Count notifications by user.
     */
    long countByUser(User user);

    /**
     * Count notifications by user and status.
     */
    long countByUserAndStatus(User user, NotificationStatus status);

    /**
     * Count notifications by user and type.
     */
    long countByUserAndType(User user, NotificationType type);
}
