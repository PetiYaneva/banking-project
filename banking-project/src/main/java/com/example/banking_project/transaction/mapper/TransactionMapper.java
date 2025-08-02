package com.example.banking_project.transaction.mapper;

import com.example.banking_project.transaction.model.Transaction;
import com.example.banking_project.web.dto.TransactionTransferResponse;
import com.example.banking_project.web.dto.TransactionTransferView;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class TransactionMapper {

    public static TransactionTransferResponse fromEntity(Transaction t) {
        return TransactionTransferResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .transactionStatus(t.getTransactionStatus().toString())
                .transactionType(t.getTransactionType().toString())
                .description(t.getDescription())
                .failureReason(t.getFailureReason())
                .createdOn(t.getCreatedOn())
                .accountId(t.getAccount().getId())
                .userId(t.getUser().getId())
                .isIncome(t.getIsIncome())
                .isExpense(t.getIsExpense())
                .build();
    }

    public TransactionTransferResponse fromView(TransactionTransferView view) {
        return TransactionTransferResponse.builder()
                .id(view.getId())
                .amount(view.getAmount())
                .currency(Currency.getInstance(view.getCurrency()))
                .transactionStatus(view.getTransactionStatus())
                .transactionType(view.getTransactionType())
                .description(view.getDescription())
                .failureReason(view.getFailureReason())
                .createdOn(view.getCreatedOn())
                .accountId(view.getAccountId())
                .userId(view.getUserId())
                .isIncome(view.getIsIncome())
                .isExpense(view.getIsExpense())
                .build();
    }
}
