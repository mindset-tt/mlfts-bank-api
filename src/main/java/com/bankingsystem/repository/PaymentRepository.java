package com.bankingsystem.repository;

import com.bankingsystem.entity.Payment;
import com.bankingsystem.entity.Account;
import com.bankingsystem.enums.PaymentStatus;
import com.bankingsystem.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity operations.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);
    
    List<Payment> findByFromAccount(Account fromAccount);
    
    List<Payment> findByFromAccountOrToAccountOrderByCreatedAtDesc(Account fromAccount, Account toAccount);
    
    List<Payment> findByFromAccountAndCreatedAtAfter(Account fromAccount, LocalDateTime createdAfter);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    List<Payment> findByPaymentType(PaymentType paymentType);
    
    @Query("SELECT p FROM Payment p WHERE p.fromAccount.id = :accountId ORDER BY p.scheduledDate DESC")
    List<Payment> findByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.scheduledDate <= :date")
    List<Payment> findByStatusAndScheduledDateBefore(@Param("status") PaymentStatus status, @Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.fromAccount.id = :accountId AND p.status = :status " +
           "AND p.processedAt BETWEEN :startDate AND :endDate")
    Long countPaymentsByAccountAndStatusAndDateRange(@Param("accountId") Long accountId,
                                                     @Param("status") PaymentStatus status,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
    
    boolean existsByPaymentReference(String paymentReference);
}
