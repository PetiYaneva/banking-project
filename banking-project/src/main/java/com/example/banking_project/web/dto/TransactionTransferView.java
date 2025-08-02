package com.example.banking_project.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface TransactionTransferView {
    UUID getId();
    BigDecimal getAmount();
    String getTransactionStatus();
    String getTransactionType();
    String getDescription();
    String getFailureReason();
    LocalDate getCreatedOn();
    String getCurrency();
    boolean getIsIncome();
    boolean getIsExpense();
    UUID getAccountId();
    UUID getUserId();
}