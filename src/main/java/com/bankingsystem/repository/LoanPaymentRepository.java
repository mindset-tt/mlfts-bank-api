package com.bankingsystem.repository;

import com.bankingsystem.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for LoanPayment entity.
 */
@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {
    
    List<LoanPayment> findByLoanIdOrderByPaymentDateDesc(Long loanId);
    
    List<LoanPayment> findByPaymentAccountIdOrderByPaymentDateDesc(Long accountId);
}
