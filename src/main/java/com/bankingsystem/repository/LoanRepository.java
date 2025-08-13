package com.bankingsystem.repository;

import com.bankingsystem.entity.Loan;
import com.bankingsystem.entity.User;
import com.bankingsystem.enums.LoanStatus;
import com.bankingsystem.enums.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Loan entity operations.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByLoanNumber(String loanNumber);
    
    List<Loan> findByUser(User user);
    
    List<Loan> findByUserAndStatus(User user, LoanStatus status);
    
    List<Loan> findByLoanType(LoanType loanType);
    
    List<Loan> findByUserIdOrderByApplicationDateDesc(Long userId);
    
    List<Loan> findByStatusOrderByApplicationDateDesc(LoanStatus status);
    
    List<Loan> findByStatus(LoanStatus status);
    
    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId")
    List<Loan> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId AND l.loanType = :loanType")
    List<Loan> findByUserIdAndLoanType(@Param("userId") Long userId, @Param("loanType") LoanType loanType);
    
    @Query("SELECT l FROM Loan l WHERE l.nextPaymentDate = :date AND l.status = 'ACTIVE'")
    List<Loan> findLoansWithPaymentDueOn(@Param("date") LocalDate date);
    
    @Query("SELECT l FROM Loan l WHERE l.nextPaymentDate < :date AND l.status = 'ACTIVE'")
    List<Loan> findOverdueLoans(@Param("date") LocalDate date);
    
    @Query("SELECT l FROM Loan l WHERE l.maturityDate BETWEEN :startDate AND :endDate")
    List<Loan> findLoansMaturingBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT l FROM Loan l WHERE l.outstandingBalance > :amount")
    List<Loan> findLoansWithBalanceGreaterThan(@Param("amount") BigDecimal amount);
    
    @Query("SELECT SUM(l.outstandingBalance) FROM Loan l WHERE l.user.id = :userId AND l.status = 'ACTIVE'")
    BigDecimal getTotalOutstandingBalanceByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user.id = :userId AND l.status = 'ACTIVE'")
    Long countActiveLoansByUserId(@Param("userId") Long userId);
    
    boolean existsByLoanNumber(String loanNumber);
}
