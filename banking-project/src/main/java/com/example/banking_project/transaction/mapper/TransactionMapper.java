package com.example.banking_project.transaction.mapper;

import com.example.banking_project.transaction.model.Transaction;
import com.example.banking_project.transaction.view.TransactionTransferView;
import com.example.banking_project.web.dto.TransactionTransferResponse;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class TransactionMapper {

    public TransactionTransferResponse fromEntity(Transaction t) {
        if (t == null) return null;

        return TransactionTransferResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .currency(t.getCurrency() != null ? t.getCurrency() : Currency.getInstance("BGN"))
                .transactionStatus(t.getTransactionStatus() != null ? t.getTransactionStatus().toString() : null)
                .transactionType(t.getTransactionType() != null ? t.getTransactionType().toString() : null)
                .description(t.getDescription())
                .failureReason(t.getFailureReason())
                .createdOn(t.getCreatedOn())
                .accountId(t.getAccount() != null ? t.getAccount().getId() : null)
                .userId(t.getUser() != null ? t.getUser().getId() : null)
                .isIncome(Boolean.TRUE.equals(t.getIsIncome()))
                .isExpense(Boolean.TRUE.equals(t.getIsExpense()))
                .build();
    }

    public TransactionTransferResponse fromView(TransactionTransferView view) {
        if (view == null) return null;

        return TransactionTransferResponse.builder()
                .id(view.getId())
                .amount(view.getAmount())
                .currency(view.getCurrency() != null ? Currency.getInstance(view.getCurrency()) : Currency.getInstance("BGN"))
                .transactionStatus(view.getTransactionStatus())
                .transactionType(view.getTransactionType())
                .description(view.getDescription())
                .failureReason(view.getFailureReason())
                .createdOn(view.getCreatedOn())
                .accountId(view.getAccountId())
                .userId(view.getUserId())
                .isIncome(Boolean.TRUE.equals(view.getIsIncome()))
                .isExpense(Boolean.TRUE.equals(view.getIsExpense()))
                .build();
    }
}
