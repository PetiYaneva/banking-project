package com.example.banking_project.account.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.web.dto.CreateAccountRequest;
import com.example.banking_project.web.dto.LoanRequest;
import com.example.banking_project.web.dto.TransferRequest;
import com.example.banking_project.web.dto.TransferResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    Account debitByIban(String iban, BigDecimal amount);

    Account creditByIban(String iban, BigDecimal amount);

    Account create(CreateAccountRequest request, UUID userId);
    List<Account> getAllAccounts();

    Account getAccountById(UUID id);
    Account getAccountByIban(String iban);

    Account updateBalance(UUID accountId, BigDecimal newBalance);
    void deleteAccount(UUID accountId);

    TransferResponse transfer(TransferRequest request);

    // Допълнително:
    List<Account> getAccountsByUserId(UUID userId);
    BigDecimal getTotalBalanceForUser(UUID userId);

     void createCreditAccount(LoanRequest request, UUID userId);
}
