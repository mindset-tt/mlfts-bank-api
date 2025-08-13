package com.bankingsystem.repository;

import com.bankingsystem.entity.LoginAttempt;
import com.bankingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for LoginAttempt entity operations.
 */
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    List<LoginAttempt> findByUsername(String username);
    
    List<LoginAttempt> findByIpAddress(String ipAddress);
    
    List<LoginAttempt> findByUser(User user);
    
    List<LoginAttempt> findByIsSuccessful(Boolean isSuccessful);
    
    @Query("SELECT la FROM LoginAttempt la WHERE la.username = :username ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findByUsernameOrderByAttemptTimeDesc(@Param("username") String username);
    
    @Query("SELECT la FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.attemptTime > :since")
    List<LoginAttempt> findRecentAttemptsByIpAddress(@Param("ipAddress") String ipAddress, 
                                                     @Param("since") LocalDateTime since);
    
    @Query("SELECT la FROM LoginAttempt la WHERE la.username = :username AND la.isSuccessful = false " +
           "AND la.attemptTime > :since")
    List<LoginAttempt> findFailedAttemptsByUsernameSince(@Param("username") String username, 
                                                         @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress " +
           "AND la.isSuccessful = false AND la.attemptTime > :since")
    Long countFailedAttemptsByIpAddressSince(@Param("ipAddress") String ipAddress, 
                                             @Param("since") LocalDateTime since);
}
