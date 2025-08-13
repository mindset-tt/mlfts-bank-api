package com.bankingsystem.service;

import com.bankingsystem.entity.AuditLog;
import com.bankingsystem.entity.User;
import com.bankingsystem.repository.AuditLogRepository;
import com.bankingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for audit logging operations.
 */
@Service
@Transactional
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    public void logUserAction(Long userId, String action, String description, String module) {
        User user = userRepository.findById(userId).orElse(null);
        logAction(action, "USER", userId.toString(), null, null, description, module, "LOW", user);
    }

    public void logSystemAction(String action, String description, String module, String severity) {
        logAction(action, "SYSTEM", null, null, null, description, module, severity, null);
    }

    public void logEntityChange(String action, String entityType, String entityId, 
                                String oldValues, String newValues, String description, 
                                String module, Long userId) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        logAction(action, entityType, entityId, oldValues, newValues, description, module, "MEDIUM", user);
    }

    public void logSecurityEvent(String action, String description, String module, String severity, Long userId) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        logAction(action, "SECURITY", userId != null ? userId.toString() : null, null, null, description, module, severity, user);
    }

    private void logAction(String action, String entityType, String entityId, 
                          String oldValues, String newValues, String description, 
                          String module, String severity, User user) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setDescription(description);
        auditLog.setModule(module);
        auditLog.setSeverity(severity);
        auditLog.setUser(user);
        auditLog.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByModule(String module) {
        return auditLogRepository.findByModule(module);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getSecurityAuditLogs() {
        return auditLogRepository.findBySeverityIn(List.of("HIGH", "CRITICAL"));
    }
}
