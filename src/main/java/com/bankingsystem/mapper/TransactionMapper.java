package com.bankingsystem.mapper;

import com.bankingsystem.dto.response.TransactionResponse;
import com.bankingsystem.entity.Transaction;
import org.springframework.stereotype.Component;

/**
 * Mapper for Transaction entity to TransactionResponse DTO.
 */
@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionReference(transaction.getTransactionReference());
        response.setTransactionType(transaction.getTransactionType());
        response.setAmount(transaction.getAmount());
        response.setStatus(transaction.getStatus());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setProcessedDate(transaction.getProcessedDate());
        response.setRunningBalance(transaction.getRunningBalance());
        response.setChannel(transaction.getChannel());
        response.setNotes(transaction.getNotes());
        response.setFeeAmount(transaction.getFeeAmount());
        
        // Set account information
        if (transaction.getFromAccount() != null) {
            response.setFromAccountId(transaction.getFromAccount().getId());
            response.setFromAccountNumber(transaction.getFromAccount().getAccountNumber());
        }
        
        if (transaction.getToAccount() != null) {
            response.setToAccountId(transaction.getToAccount().getId());
            response.setToAccountNumber(transaction.getToAccount().getAccountNumber());
        }
        
        // Set user information
        if (transaction.getInitiatedBy() != null) {
            response.setInitiatedByUserId(transaction.getInitiatedBy().getId());
            response.setInitiatedByUsername(transaction.getInitiatedBy().getUsername());
        }
        
        return response;
    }
}
