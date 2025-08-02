package com.example.banking_project.web.dto;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.transaction.model.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Data
@Builder
public class TransactionRequest {
    private BigDecimal amount;
    private Currency currency;
    private String description;
    private String iban;
    private Account account;
    private UUID userId;
    private TransactionType transactionType;
}
