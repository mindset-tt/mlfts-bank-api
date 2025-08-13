package com.bankingsystem.service;

import com.bankingsystem.dto.request.AccountCreationRequest;
import com.bankingsystem.dto.response.AccountResponse;
import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.User;
import com.bankingsystem.enums.AccountType;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.util.NumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for account-related operations.
 */
@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Value("${banking.account.minimum-balance.checking:100.00}")
    private BigDecimal checkingMinBalance;

    @Value("${banking.account.minimum-balance.savings:500.00}")
    private BigDecimal savingsMinBalance;

    @Value("${banking.account.minimum-balance.business:1000.00}")
    private BigDecimal businessMinBalance;

    public AccountResponse createAccount(Long userId, AccountCreationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        // Generate unique account number
        String accountNumber;
        do {
            accountNumber = NumberGenerator.generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());
        account.setAvailableBalance(request.getInitialBalance());
        account.setMinimumBalance(getMinimumBalanceForAccountType(request.getAccountType()));
        account.setOverdraftLimit(request.getOverdraftLimit());
        account.setUser(user);
        account.setOpenedDate(LocalDateTime.now());

        // Set default interest rates and fees based on account type
        setDefaultAccountSettings(account);

        Account savedAccount = accountRepository.save(account);

        // Log account creation
        auditService.logUserAction(userId, "ACCOUNT_CREATED", 
                "Account created: " + accountNumber, "ACCOUNT");

        return convertToAccountResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId.toString()));
        return convertToAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountNumber));
        return convertToAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::convertToAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getActiveAccountsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        return accountRepository.findByUserAndIsActive(user, true).stream()
                .map(this::convertToAccountResponse)
                .collect(Collectors.toList());
    }

    public AccountResponse updateAccount(Long accountId, AccountCreationRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId.toString()));

        String oldValues = account.toString();
        
        account.setOverdraftLimit(request.getOverdraftLimit());
        
        Account updatedAccount = accountRepository.save(account);

        // Log account update
        auditService.logEntityChange("ACCOUNT_UPDATED", "Account", accountId.toString(), 
                oldValues, updatedAccount.toString(), "Account settings updated", "ACCOUNT", 
                account.getUser().getId());

        return convertToAccountResponse(updatedAccount);
    }

    public void closeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId.toString()));

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance");
        }

        account.setIsActive(false);
        account.setClosedDate(LocalDateTime.now());
        accountRepository.save(account);

        // Log account closure
        auditService.logUserAction(account.getUser().getId(), "ACCOUNT_CLOSED", 
                "Account closed: " + account.getAccountNumber(), "ACCOUNT");
    }

    public AccountResponse freezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId.toString()));

        account.setIsFrozen(true);
        Account updatedAccount = accountRepository.save(account);

        // Log account freeze
        auditService.logSecurityEvent("ACCOUNT_FROZEN", 
                "Account frozen: " + account.getAccountNumber(), "SECURITY", "HIGH", 
                account.getUser().getId());

        return convertToAccountResponse(updatedAccount);
    }

    public AccountResponse unfreezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId.toString()));

        account.setIsFrozen(false);
        Account updatedAccount = accountRepository.save(account);

        // Log account unfreeze
        auditService.logSecurityEvent("ACCOUNT_UNFROZEN", 
                "Account unfrozen: " + account.getAccountNumber(), "SECURITY", "MEDIUM", 
                account.getUser().getId());

        return convertToAccountResponse(updatedAccount);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(Long userId) {
        BigDecimal total = accountRepository.getTotalBalanceByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    private BigDecimal getMinimumBalanceForAccountType(AccountType accountType) {
        return switch (accountType) {
            case CHECKING -> checkingMinBalance;
            case SAVINGS, MONEY_MARKET -> savingsMinBalance;
            case BUSINESS -> businessMinBalance;
            case INVESTMENT -> BigDecimal.ZERO;
        };
    }

    private void setDefaultAccountSettings(Account account) {
        switch (account.getAccountType()) {
            case CHECKING:
                account.setInterestRate(new BigDecimal("0.0050")); // 0.5% annual
                account.setMonthlyMaintenanceFee(new BigDecimal("10.00"));
                break;
            case SAVINGS:
                account.setInterestRate(new BigDecimal("0.0200")); // 2.0% annual
                account.setMonthlyMaintenanceFee(BigDecimal.ZERO);
                break;
            case BUSINESS:
                account.setInterestRate(new BigDecimal("0.0100")); // 1.0% annual
                account.setMonthlyMaintenanceFee(new BigDecimal("25.00"));
                break;
            case MONEY_MARKET:
                account.setInterestRate(new BigDecimal("0.0150")); // 1.5% annual
                account.setMonthlyMaintenanceFee(new BigDecimal("15.00"));
                break;
            case INVESTMENT:
                account.setInterestRate(BigDecimal.ZERO);
                account.setMonthlyMaintenanceFee(new BigDecimal("5.00"));
                break;
        }
    }

    private AccountResponse convertToAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setAvailableBalance(account.getAvailableBalance());
        response.setMinimumBalance(account.getMinimumBalance());
        response.setIsActive(account.getIsActive());
        response.setIsFrozen(account.getIsFrozen());
        response.setOpenedDate(account.getOpenedDate());
        response.setClosedDate(account.getClosedDate());
        response.setInterestRate(account.getInterestRate());
        response.setOverdraftLimit(account.getOverdraftLimit());
        response.setMonthlyMaintenanceFee(account.getMonthlyMaintenanceFee());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        return response;
    }
}
