package com.bankingsystem.repository;

import com.bankingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.isEnabled = :enabled")
    List<User> findByIsEnabled(@Param("enabled") Boolean enabled);
    
    @Query("SELECT u FROM User u WHERE u.kycVerified = :verified")
    List<User> findByKycVerified(@Param("verified") Boolean verified);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date")
    List<User> findUsersInactiveSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :attempts")
    List<User> findUsersWithFailedAttempts(@Param("attempts") Integer attempts);
    
    @Query("SELECT u FROM User u WHERE u.role = com.bankingsystem.enums.UserRole.CUSTOMER")
    List<User> findAllCustomers();
    
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(@Param("query") String query);
}
