package com.bankingsystem.repository;

import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BillPayment entity operations.
 */
@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    /**
     * Find bill payments by account ordered by creation date descending.
     */
    List<BillPayment> findByAccountOrderByCreatedAtDesc(Account account);

    /**
     * Find bill payment by payment reference.
     */
    Optional<BillPayment> findByPaymentReference(String paymentReference);

    /**
     * Find bill payments by account and biller code.
     */
    List<BillPayment> findByAccountAndBillerCodeOrderByCreatedAtDesc(Account account, String billerCode);
}
