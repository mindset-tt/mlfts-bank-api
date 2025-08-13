package com.bankingsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bankingsystem.service.ReportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for generating bank reports in PDF format.
 * Provides endpoints for various banking reports including statements and summaries.
 */
@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Generate Account Statement PDF
     * 
     * @param accountId Account ID
     * @param fromDate Start date (yyyy-MM-dd)
     * @param toDate End date (yyyy-MM-dd)
     * @return PDF file as ByteArrayResource
     */
    @GetMapping("/account-statement/{accountId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('TELLER') or hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> generateAccountStatement(
            @PathVariable Long accountId,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        
        try {
            log.info("Generating account statement for account: {}, from: {}, to: {}", 
                    accountId, fromDate, toDate);
            
            LocalDateTime from = LocalDateTime.parse(fromDate + "T00:00:00");
            LocalDateTime to = LocalDateTime.parse(toDate + "T23:59:59");
            
            ByteArrayResource resource = reportService.generateAccountStatement(accountId, from, to);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=account_statement_" + accountId + "_" + fromDate + "_" + toDate + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
            
            log.info("Account statement generated successfully for account: {}", accountId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error generating account statement for account: {}", accountId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate Monthly Statement PDF
     * 
     * @param userId User ID
     * @param year Year (e.g., 2025)
     * @param month Month (1-12)
     * @return PDF file as ByteArrayResource
     */
    @GetMapping("/monthly-statement/{userId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('TELLER') or hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> generateMonthlyStatement(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        
        try {
            log.info("Generating monthly statement for user: {}, year: {}, month: {}", 
                    userId, year, month);
            
            ByteArrayResource resource = reportService.generateMonthlyStatement(userId, year, month);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=monthly_statement_" + userId + "_" + year + "_" + String.format("%02d", month) + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
            
            log.info("Monthly statement generated successfully for user: {}", userId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error generating monthly statement for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate Loan Statement PDF
     * 
     * @param loanId Loan ID
     * @return PDF file as ByteArrayResource
     */
    @GetMapping("/loan-statement/{loanId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('TELLER') or hasRole('LOAN_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> generateLoanStatement(@PathVariable Long loanId) {
        
        try {
            log.info("Generating loan statement for loan: {}", loanId);
            
            ByteArrayResource resource = reportService.generateLoanStatement(loanId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=loan_statement_" + loanId + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
            
            log.info("Loan statement generated successfully for loan: {}", loanId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error generating loan statement for loan: {}", loanId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate Credit Card Statement PDF
     * 
     * @param cardId Card ID
     * @param year Year (e.g., 2025)
     * @param month Month (1-12)
     * @return PDF file as ByteArrayResource
     */
    @GetMapping("/credit-card-statement/{cardId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('TELLER') or hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> generateCreditCardStatement(
            @PathVariable Long cardId,
            @RequestParam int year,
            @RequestParam int month) {
        
        try {
            log.info("Generating credit card statement for card: {}, year: {}, month: {}", 
                    cardId, year, month);
            
            ByteArrayResource resource = reportService.generateCreditCardStatement(cardId, year, month);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=credit_card_statement_" + cardId + "_" + year + "_" + String.format("%02d", month) + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
            
            log.info("Credit card statement generated successfully for card: {}", cardId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error generating credit card statement for card: {}", cardId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate Bank Summary Report PDF (Admin only)
     * 
     * @param year Year for the report
     * @param month Month for the report (optional)
     * @return PDF file as ByteArrayResource
     */
    @GetMapping("/bank-summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ByteArrayResource> generateBankSummaryReport(
            @RequestParam int year,
            @RequestParam(required = false) Integer month) {
        
        try {
            log.info("Generating bank summary report for year: {}, month: {}", year, month);
            
            // This would be implemented in ReportService
            // For now, return a simple response
            
            HttpHeaders headers = new HttpHeaders();
            String filename = "bank_summary_" + year;
            if (month != null) {
                filename += "_" + String.format("%02d", month);
            }
            filename += ".pdf";
            
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
            
            // TODO: Implement bank summary report generation
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .headers(headers)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error generating bank summary report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check for reports service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Reports service is running - " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
