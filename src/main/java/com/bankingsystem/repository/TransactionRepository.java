package com.bankingsystem.repository;

import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.Transaction;
import com.bankingsystem.enums.TransactionStatus;
import com.bankingsystem.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    List<Transaction> findByFromAccount(Account fromAccount);
    
    List<Transaction> findByToAccount(Account toAccount);
    
    List<Transaction> findByFromAccountOrToAccount(Account fromAccount, Account toAccount);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    List<Transaction> findByTransactionType(TransactionType transactionType);
    
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.transactionDate DESC")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.initiatedBy.id = :userId ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "AND t.transactionDate BETWEEN :fromDate AND :toDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountAndDateRange(@Param("accountId") Long accountId, 
                                              @Param("fromDate") LocalDateTime fromDate, 
                                              @Param("toDate") LocalDateTime toDate);
    
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountIdAndDateRange(@Param("accountId") Long accountId, 
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.amount > :amount")
    List<Transaction> findLargeTransactions(@Param("amount") BigDecimal amount);
    
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' AND t.transactionDate < :cutoffTime")
    List<Transaction> findPendingTransactionsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromAccount.id = :accountId " +
           "AND t.transactionType = :transactionType AND t.status = 'COMPLETED' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByAccountAndTypeAndDateRange(@Param("accountId") Long accountId,
                                                     @Param("transactionType") TransactionType transactionType,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromAccount.id = :accountId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    Long countTransactionsByAccountAndDateRange(@Param("accountId") Long accountId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
    
    boolean existsByTransactionReference(String transactionReference);
}
