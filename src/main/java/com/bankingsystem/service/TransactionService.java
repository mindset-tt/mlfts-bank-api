package com.bankingsystem.service;

import com.bankingsystem.dto.response.TransactionResponse;
import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.Transaction;
import com.bankingsystem.enums.TransactionStatus;
import com.bankingsystem.enums.TransactionType;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.exception.InsufficientFundsException;
import com.bankingsystem.exception.InvalidTransactionException;
import com.bankingsystem.mapper.TransactionMapper;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling transaction operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse deposit(Long accountId, BigDecimal amount, String description) {
        log.info("Processing deposit for account: {} amount: {}", accountId, amount);
        
        Account account = getAccountById(accountId);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Deposit amount must be greater than zero");
        }
        
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setToAccount(account);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setProcessedDate(LocalDateTime.now());
        
        // Update account balance
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        transaction.setRunningBalance(newBalance);
        
        accountRepository.save(account);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        auditService.logUserAction(
            account.getUser().getId(), 
            "DEPOSIT", 
            "Deposit of " + amount + " to account " + account.getAccountNumber(),
            "TRANSACTION"
        );
        
        log.info("Deposit completed: {}", savedTransaction.getTransactionReference());
        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    public TransactionResponse withdraw(Long accountId, BigDecimal amount, String description) {
        log.info("Processing withdrawal for account: {} amount: {}", accountId, amount);
        
        Account account = getAccountById(accountId);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be greater than zero");
        }
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setFromAccount(account);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setProcessedDate(LocalDateTime.now());
        
        // Update account balance
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        transaction.setRunningBalance(newBalance);
        
        accountRepository.save(account);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        auditService.logUserAction(
            account.getUser().getId(), 
            "WITHDRAWAL", 
            "Withdrawal of " + amount + " from account " + account.getAccountNumber(),
            "TRANSACTION"
        );
        
        log.info("Withdrawal completed: {}", savedTransaction.getTransactionReference());
        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    public List<TransactionResponse> transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        log.info("Processing transfer from account: {} to account: {} amount: {}", fromAccountId, toAccountId, amount);
        
        Account fromAccount = getAccountById(fromAccountId);
        Account toAccount = getAccountById(toAccountId);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be greater than zero");
        }
        
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in source account");
        }
        
        if (fromAccountId.equals(toAccountId)) {
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }
        
        String transferRef = generateTransactionReference();
        
        // Debit transaction
        Transaction debitTransaction = new Transaction();
        debitTransaction.setTransactionReference(transferRef + "-DEBIT");
        debitTransaction.setTransactionType(TransactionType.TRANSFER);
        debitTransaction.setAmount(amount);
        debitTransaction.setDescription("Transfer to " + toAccount.getAccountNumber() + " - " + description);
        debitTransaction.setFromAccount(fromAccount);
        debitTransaction.setToAccount(toAccount);
        debitTransaction.setStatus(TransactionStatus.COMPLETED);
        debitTransaction.setTransactionDate(LocalDateTime.now());
        debitTransaction.setProcessedDate(LocalDateTime.now());
        
        // Credit transaction
        Transaction creditTransaction = new Transaction();
        creditTransaction.setTransactionReference(transferRef + "-CREDIT");
        creditTransaction.setTransactionType(TransactionType.TRANSFER);
        creditTransaction.setAmount(amount);
        creditTransaction.setDescription("Transfer from " + fromAccount.getAccountNumber() + " - " + description);
        creditTransaction.setFromAccount(fromAccount);
        creditTransaction.setToAccount(toAccount);
        creditTransaction.setStatus(TransactionStatus.COMPLETED);
        creditTransaction.setTransactionDate(LocalDateTime.now());
        creditTransaction.setProcessedDate(LocalDateTime.now());
        
        // Update balances
        BigDecimal fromNewBalance = fromAccount.getBalance().subtract(amount);
        BigDecimal toNewBalance = toAccount.getBalance().add(amount);
        
        fromAccount.setBalance(fromNewBalance);
        toAccount.setBalance(toNewBalance);
        
        debitTransaction.setRunningBalance(fromNewBalance);
        creditTransaction.setRunningBalance(toNewBalance);
        
        accountRepository.saveAll(Arrays.asList(fromAccount, toAccount));
        List<Transaction> transactions = transactionRepository.saveAll(Arrays.asList(debitTransaction, creditTransaction));
        
        auditService.logUserAction(
            fromAccount.getUser().getId(), 
            "TRANSFER_OUT", 
            "Transfer of " + amount + " to account " + toAccount.getAccountNumber(),
            "TRANSACTION"
        );
        
        auditService.logUserAction(
            toAccount.getUser().getId(), 
            "TRANSFER_IN", 
            "Transfer of " + amount + " from account " + fromAccount.getAccountNumber(),
            "TRANSACTION"
        );
        
        log.info("Transfer completed: {}", transferRef);
        return transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));
        return transactionMapper.toResponse(transaction);
    }

    public Page<TransactionResponse> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        return transactions.map(transactionMapper::toResponse);
    }

    public Page<TransactionResponse> getTransactionsByUserId(Long userId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByUserId(userId, pageable);
        return transactions.map(transactionMapper::toResponse);
    }

    public Page<TransactionResponse> getTransactionsByType(TransactionType type, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findByTransactionType(type);
        List<TransactionResponse> responses = transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
        
        // Simple pagination implementation
        int start = Math.min((int) pageable.getOffset(), responses.size());
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<TransactionResponse> pageContent = responses.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    public List<TransactionResponse> getTransactionsByDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);
        return transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));
    }

    private String generateTransactionReference() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
