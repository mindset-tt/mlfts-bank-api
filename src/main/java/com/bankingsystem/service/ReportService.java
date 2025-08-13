package com.bankingsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.Transaction;
import com.bankingsystem.entity.Loan;
import com.bankingsystem.entity.User;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.TransactionRepository;
import com.bankingsystem.repository.LoanRepository;
import com.bankingsystem.repository.UserRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    /**
     * Generate Account Statement Report
     */
    public ByteArrayResource generateAccountStatement(Long accountId, LocalDateTime fromDate, LocalDateTime toDate) {
        try {
            // Get account details
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            
            // Get transactions for the period
            List<Transaction> transactions = transactionRepository
                    .findByAccountAndDateRange(accountId, fromDate, toDate);
            
            // Prepare report data
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("accountNumber", account.getAccountNumber());
            parameters.put("accountType", account.getAccountType().toString());
            parameters.put("customerName", account.getUser().getFirstName() + " " + account.getUser().getLastName());
            parameters.put("fromDate", fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            parameters.put("toDate", toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            parameters.put("currentBalance", account.getBalance());
            parameters.put("reportDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            // Transaction data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(transactions);
            
            return generateReport("account_statement.jrxml", parameters, dataSource);
            
        } catch (Exception e) {
            log.error("Error generating account statement: ", e);
            throw new RuntimeException("Failed to generate account statement", e);
        }
    }

    /**
     * Generate Monthly Bank Statement
     */
    public ByteArrayResource generateMonthlyStatement(Long userId, int year, int month) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Account> accounts = accountRepository.findByUserId(userId);
            
            LocalDateTime fromDate = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime toDate = fromDate.plusMonths(1).minusSeconds(1);
            
            // Prepare data for each account
            List<Map<String, Object>> accountData = new ArrayList<>();
            BigDecimal totalBalance = BigDecimal.ZERO;
            
            for (Account account : accounts) {
                List<Transaction> transactions = transactionRepository
                        .findByAccountAndDateRange(account.getId(), fromDate, toDate);
                
                Map<String, Object> accData = new HashMap<>();
                accData.put("accountNumber", account.getAccountNumber());
                accData.put("accountType", account.getAccountType().toString());
                accData.put("currentBalance", account.getBalance());
                accData.put("transactionCount", transactions.size());
                
                BigDecimal monthlyDebit = transactions.stream()
                        .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) < 0)
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal monthlyCredit = transactions.stream()
                        .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                accData.put("monthlyDebit", monthlyDebit.abs());
                accData.put("monthlyCredit", monthlyCredit);
                
                accountData.add(accData);
                totalBalance = totalBalance.add(account.getBalance());
            }
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("customerName", user.getFirstName() + " " + user.getLastName());
            parameters.put("customerId", user.getId());
            parameters.put("reportMonth", String.format("%02d/%d", month, year));
            parameters.put("totalBalance", totalBalance);
            parameters.put("accountCount", accounts.size());
            parameters.put("reportDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(accountData);
            
            return generateReport("monthly_statement.jrxml", parameters, dataSource);
            
        } catch (Exception e) {
            log.error("Error generating monthly statement: ", e);
            throw new RuntimeException("Failed to generate monthly statement", e);
        }
    }

    /**
     * Generate Loan Statement Report
     */
    public ByteArrayResource generateLoanStatement(Long loanId) {
        try {
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new RuntimeException("Loan not found"));
            
            // Calculate loan details
            BigDecimal totalPaid = loan.getPrincipalAmount().subtract(loan.getOutstandingBalance());
            int remainingMonths = calculateRemainingMonths(loan);
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("loanNumber", loan.getLoanNumber());
            parameters.put("loanType", loan.getLoanType().toString());
            parameters.put("customerName", loan.getUser().getFirstName() + " " + loan.getUser().getLastName());
            parameters.put("loanAmount", loan.getPrincipalAmount());
            parameters.put("interestRate", loan.getInterestRate());
            parameters.put("termMonths", loan.getTermInMonths());
            parameters.put("monthlyPayment", loan.getMonthlyPayment());
            parameters.put("outstandingBalance", loan.getOutstandingBalance());
            parameters.put("totalPaid", totalPaid);
            parameters.put("remainingMonths", remainingMonths);
            parameters.put("applicationDate", loan.getApplicationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            parameters.put("reportDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            // Create empty data source for simple report
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.emptyList());
            
            return generateReport("loan_statement.jrxml", parameters, dataSource);
            
        } catch (Exception e) {
            log.error("Error generating loan statement: ", e);
            throw new RuntimeException("Failed to generate loan statement", e);
        }
    }

    /**
     * Generate Credit Card Statement
     */
    public ByteArrayResource generateCreditCardStatement(Long cardId, int year, int month) {
        try {
            // This would be implemented when Card transactions are available
            // For now, return a placeholder
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportMonth", String.format("%02d/%d", month, year));
            parameters.put("reportDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.emptyList());
            
            return generateReport("credit_card_statement.jrxml", parameters, dataSource);
            
        } catch (Exception e) {
            log.error("Error generating credit card statement: ", e);
            throw new RuntimeException("Failed to generate credit card statement", e);
        }
    }

    /**
     * Core method to generate JasperReports PDF
     */
    private ByteArrayResource generateReport(String templateName, Map<String, Object> parameters, 
                                           JRBeanCollectionDataSource dataSource) throws Exception {
        
        // Load the report template
        InputStream templateStream = new ClassPathResource("reports/" + templateName).getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
        
        // Fill the report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        
        // Export to PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        exporter.exportReport();
        
        return new ByteArrayResource(outputStream.toByteArray());
    }

    private int calculateRemainingMonths(Loan loan) {
        // Simplified calculation - in real scenario, this would consider payment history
        if (loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        
        BigDecimal remainingAmount = loan.getOutstandingBalance();
        BigDecimal monthlyPayment = loan.getMonthlyPayment();
        
        if (monthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
            return loan.getTermInMonths();
        }
        
        return remainingAmount.divide(monthlyPayment, 0, java.math.RoundingMode.UP).intValue();
    }
}
