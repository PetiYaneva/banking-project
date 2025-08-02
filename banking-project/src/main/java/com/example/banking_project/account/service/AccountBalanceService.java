package com.example.banking_project.account.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.transaction.model.TransactionType;

import java.math.BigDecimal;

public interface AccountBalanceService {
    void updateBalance(String iban, BigDecimal amount, TransactionType type);

    Account getAccountByIban(String iban);
}