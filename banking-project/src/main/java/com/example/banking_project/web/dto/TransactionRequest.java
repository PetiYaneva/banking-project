package com.example.banking_project.web.dto;

import com.example.banking_project.transaction.model.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Data
public class TransactionRequest {
    private BigDecimal amount;
    private Currency currency;
    private String description;
    private String receiver;
    private String sender;
    private UUID accountId;
    private UUID userId;
    private Boolean isIncome;
    private Boolean isExpense;
    private TransactionType transactionType;
}
