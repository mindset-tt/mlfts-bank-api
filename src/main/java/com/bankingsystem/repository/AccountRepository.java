package com.bankingsystem.repository;

import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.User;
import com.bankingsystem.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity operations.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);
    
    List<Account> findByUser(User user);
    
    List<Account> findByUserAndIsActive(User user, Boolean isActive);
    
    List<Account> findByAccountType(AccountType accountType);
    
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    List<Account> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.accountType = :accountType")
    List<Account> findByUserIdAndAccountType(@Param("userId") Long userId, @Param("accountType") AccountType accountType);
    
    @Query("SELECT a FROM Account a WHERE a.balance < a.minimumBalance")
    List<Account> findAccountsBelowMinimumBalance();
    
    @Query("SELECT a FROM Account a WHERE a.balance > :amount")
    List<Account> findAccountsWithBalanceGreaterThan(@Param("amount") BigDecimal amount);
    
    @Query("SELECT a FROM Account a WHERE a.isFrozen = true")
    List<Account> findFrozenAccounts();
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    Long countActiveAccountsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
    
    boolean existsByAccountNumber(String accountNumber);
}
