package com.bankingsystem.repository;

import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.Card;
import com.bankingsystem.entity.User;
import com.bankingsystem.enums.CardStatus;
import com.bankingsystem.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Card entity operations.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByCardNumber(String cardNumber);
    
    List<Card> findByUser(User user);
    
    List<Card> findByAccount(Account account);
    
    List<Card> findByUserAndStatus(User user, CardStatus status);
    
    List<Card> findByUserIdOrderByIssuedDateDesc(Long userId);
    
    List<Card> findByCardType(CardType cardType);
    
    List<Card> findByStatus(CardStatus status);
    
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    List<Card> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Card c WHERE c.account.id = :accountId")
    List<Card> findByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.cardType = :cardType")
    List<Card> findByUserIdAndCardType(@Param("userId") Long userId, @Param("cardType") CardType cardType);
    
    @Query("SELECT c FROM Card c WHERE c.expiryDate < :date")
    List<Card> findExpiredCards(@Param("date") LocalDate date);
    
    @Query("SELECT c FROM Card c WHERE c.expiryDate BETWEEN :startDate AND :endDate")
    List<Card> findCardsExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT c FROM Card c WHERE c.status = 'ACTIVE' AND c.expiryDate > CURRENT_DATE")
    List<Card> findActiveCards();
    
    @Query("SELECT COUNT(c) FROM Card c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    Long countActiveCardsByUserId(@Param("userId") Long userId);
    
    boolean existsByCardNumber(String cardNumber);
}
