package com.bankingsystem.service;

import com.bankingsystem.dto.request.CardCreationRequest;
import com.bankingsystem.dto.request.CardTransactionRequest;
import com.bankingsystem.dto.response.CardResponse;
import com.bankingsystem.dto.response.CardTransactionResponse;
import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.Card;
import com.bankingsystem.entity.CardTransaction;
import com.bankingsystem.entity.User;
import com.bankingsystem.enums.CardStatus;
import com.bankingsystem.enums.CardType;
import com.bankingsystem.enums.TransactionStatus;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.exception.InvalidOperationException;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.CardRepository;
import com.bankingsystem.repository.CardTransactionRepository;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.util.NumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service class for card-related operations.
 */
@Service
@Transactional
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CardResponse createCard(Long userId, CardCreationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.getAccountId().toString()));

        if (!account.getUser().getId().equals(userId)) {
            throw new InvalidOperationException("Account does not belong to user");
        }

        // Create new card
        Card card = new Card();
        card.setCardNumber(generateCardNumber());
        card.setCardType(request.getCardType());
        card.setCardholderName(user.getFirstName() + " " + user.getLastName());
        card.setCvv(generateCvv());
        card.setPin(passwordEncoder.encode(request.getPin()));
        card.setExpiryDate(LocalDate.now().plusYears(4));
        card.setUser(user);
        card.setAccount(account);
        card.setStatus(CardStatus.PENDING_ACTIVATION);
        card.setIssuedDate(LocalDateTime.now());
        card.setContactlessEnabled(true);
        card.setOnlineTransactionsEnabled(true);
        card.setInternationalTransactionsEnabled(false);

        // Set limits based on card type
        if (request.getCardType() == CardType.CREDIT) {
            card.setCreditLimit(request.getCreditLimit());
            card.setAvailableCredit(request.getCreditLimit());
            card.setInterestRate(BigDecimal.valueOf(18.99)); // Default APR
        }

        card.setDailyLimit(BigDecimal.valueOf(5000.00));
        card.setMonthlyLimit(BigDecimal.valueOf(50000.00));

        Card savedCard = cardRepository.save(card);

        auditService.logUserAction(userId, "CARD_CREATED", 
            "Card created: " + savedCard.getCardType() + " ending in " + 
            savedCard.getCardNumber().substring(savedCard.getCardNumber().length() - 4), "CARD");

        return convertToResponse(savedCard);
    }

    public CardResponse activateCard(Long cardId, String pin) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardId.toString()));

        if (card.getStatus() != CardStatus.PENDING_ACTIVATION) {
            throw new InvalidOperationException("Card cannot be activated in current status: " + card.getStatus());
        }

        if (!passwordEncoder.matches(pin, card.getPin())) {
            throw new InvalidOperationException("Invalid PIN");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setActivatedDate(LocalDateTime.now());
        Card savedCard = cardRepository.save(card);

        auditService.logSecurityEvent("CARD_ACTIVATED", 
            "Card activated: ending in " + card.getCardNumber().substring(card.getCardNumber().length() - 4), 
            "SECURITY", "MEDIUM", card.getUser().getId());

        return convertToResponse(savedCard);
    }

    public CardResponse blockCard(Long cardId, String reason) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardId.toString()));

        if (card.getStatus() == CardStatus.BLOCKED || card.getStatus() == CardStatus.CANCELLED) {
            throw new InvalidOperationException("Card is already blocked or cancelled");
        }

        card.setStatus(CardStatus.BLOCKED);
        card.setBlockedDate(LocalDateTime.now());
        Card savedCard = cardRepository.save(card);

        auditService.logSecurityEvent("CARD_BLOCKED", 
            "Card blocked: ending in " + card.getCardNumber().substring(card.getCardNumber().length() - 4) + 
            " - Reason: " + reason, "SECURITY", "HIGH", card.getUser().getId());

        return convertToResponse(savedCard);
    }

    public CardResponse unblockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardId.toString()));

        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new InvalidOperationException("Card is not blocked");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setBlockedDate(null);
        Card savedCard = cardRepository.save(card);

        auditService.logSecurityEvent("CARD_UNBLOCKED", 
            "Card unblocked: ending in " + card.getCardNumber().substring(card.getCardNumber().length() - 4), 
            "SECURITY", "MEDIUM", card.getUser().getId());

        return convertToResponse(savedCard);
    }

    public CardTransactionResponse processCardTransaction(CardTransactionRequest request) {
        Card card = cardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "Card not found"));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidOperationException("Card is not active");
        }

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Card has expired");
        }

        // Validate transaction amount against limits
        if (request.getAmount().compareTo(card.getDailyLimit()) > 0) {
            throw new InvalidOperationException("Transaction exceeds daily limit");
        }

        Account account = card.getAccount();

        // Check if sufficient funds/credit available
        if (card.getCardType() == CardType.DEBIT) {
            if (account.getAvailableBalance().compareTo(request.getAmount()) < 0) {
                throw new InvalidOperationException("Insufficient funds");
            }
        } else if (card.getCardType() == CardType.CREDIT) {
            if (card.getAvailableCredit().compareTo(request.getAmount()) < 0) {
                throw new InvalidOperationException("Insufficient credit");
            }
        }

        // Create card transaction
        CardTransaction transaction = new CardTransaction();
        transaction.setCard(card);
        transaction.setAmount(request.getAmount());
        transaction.setMerchantName(request.getMerchantName());
        transaction.setMerchantCategory(request.getMerchantCategory());
        transaction.setLocation(request.getLocation());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setDescription(request.getDescription());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionReference(NumberGenerator.generateTransactionReference());
        transaction.setAuthorizationCode(NumberGenerator.generateAuthorizationCode());
        transaction.setIsContactless(request.getIsContactless());
        transaction.setIsOnline(request.getIsOnline());
        transaction.setIsInternational(request.getIsInternational());
        transaction.setProcessedDate(LocalDateTime.now());

        // Update account/card balances
        if (card.getCardType() == CardType.DEBIT) {
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            account.setAvailableBalance(account.getAvailableBalance().subtract(request.getAmount()));
            accountRepository.save(account);
        } else if (card.getCardType() == CardType.CREDIT) {
            card.setAvailableCredit(card.getAvailableCredit().subtract(request.getAmount()));
            cardRepository.save(card);
        }

        CardTransaction savedTransaction = cardTransactionRepository.save(transaction);

        auditService.logUserAction(card.getUser().getId(), "CARD_TRANSACTION", 
            "Card transaction: " + request.getAmount() + " at " + request.getMerchantName(), "CARD");

        return convertToTransactionResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public CardResponse getCardById(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardId.toString()));
        return convertToResponse(card);
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getUserCards(Long userId) {
        List<Card> cards = cardRepository.findByUserIdOrderByIssuedDateDesc(userId);
        return cards.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CardTransactionResponse> getCardTransactions(Long cardId) {
        List<CardTransaction> transactions = cardTransactionRepository.findByCardIdOrderByTransactionDateDesc(cardId);
        return transactions.stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    private String generateCardNumber() {
        // Generate a 16-digit card number (simplified)
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder("4");  // Start with 4 for Visa-like
        
        for (int i = 1; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }
        
        return cardNumber.toString();
    }

    private String generateCvv() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }

    private CardResponse convertToResponse(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setCardNumber(maskCardNumber(card.getCardNumber()));
        response.setCardType(card.getCardType());
        response.setCardholderName(card.getCardholderName());
        response.setExpiryDate(card.getExpiryDate());
        response.setStatus(card.getStatus());
        response.setIssuedDate(card.getIssuedDate());
        response.setActivatedDate(card.getActivatedDate());
        response.setCreditLimit(card.getCreditLimit());
        response.setAvailableCredit(card.getAvailableCredit());
        response.setDailyLimit(card.getDailyLimit());
        response.setMonthlyLimit(card.getMonthlyLimit());
        response.setContactlessEnabled(card.getContactlessEnabled());
        response.setOnlineTransactionsEnabled(card.getOnlineTransactionsEnabled());
        response.setInternationalTransactionsEnabled(card.getInternationalTransactionsEnabled());
        return response;
    }

    private CardTransactionResponse convertToTransactionResponse(CardTransaction transaction) {
        CardTransactionResponse response = new CardTransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setMerchantName(transaction.getMerchantName());
        response.setMerchantCategory(transaction.getMerchantCategory());
        response.setLocation(transaction.getLocation());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setTransactionType(transaction.getTransactionType());
        response.setDescription(transaction.getDescription());
        response.setStatus(transaction.getStatus());
        response.setTransactionReference(transaction.getTransactionReference());
        response.setAuthorizationCode(transaction.getAuthorizationCode());
        response.setIsContactless(transaction.getIsContactless());
        response.setIsOnline(transaction.getIsOnline());
        response.setIsInternational(transaction.getIsInternational());
        return response;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() < 4) return cardNumber;
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
