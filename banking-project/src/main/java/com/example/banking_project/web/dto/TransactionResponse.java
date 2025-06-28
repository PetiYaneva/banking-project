package com.example.banking_project.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private BigDecimal amount;
    private String transactionStatus;
    private String transactionType;
    private String sender;
    private String receiver;
    private String description;
    private String failureReason;
    private LocalDate createdOn;
    private Currency currency;
    private boolean isIncome;
    private boolean isExpense;
    private UUID accountId;
    private UUID userId;
    private UUID incomeId;
    private UUID expenseId;
}