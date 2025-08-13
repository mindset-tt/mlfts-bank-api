package com.bankingsystem.repository;

import com.bankingsystem.entity.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for CardTransaction entity.
 */
@Repository
public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {
    
    List<CardTransaction> findByCardIdOrderByTransactionDateDesc(Long cardId);
    
    List<CardTransaction> findByCardUserIdOrderByTransactionDateDesc(Long userId);
}
