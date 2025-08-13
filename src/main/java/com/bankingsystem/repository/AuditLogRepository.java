package com.bankingsystem.repository;

import com.bankingsystem.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity operations.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByAction(String action);
    
    List<AuditLog> findByEntityType(String entityType);
    
    List<AuditLog> findByModule(String module);
    
    List<AuditLog> findBySeverity(String severity);
    
    @Query("SELECT al FROM AuditLog al WHERE al.user.id = :userId ORDER BY al.timestamp DESC")
    List<AuditLog> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId);
    
    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY al.timestamp DESC")
    List<AuditLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT al FROM AuditLog al WHERE al.action = :action AND al.timestamp > :since")
    List<AuditLog> findByActionSince(@Param("action") String action, @Param("since") LocalDateTime since);
    
    @Query("SELECT al FROM AuditLog al WHERE al.severity IN :severities ORDER BY al.timestamp DESC")
    List<AuditLog> findBySeverityIn(@Param("severities") List<String> severities);
}
