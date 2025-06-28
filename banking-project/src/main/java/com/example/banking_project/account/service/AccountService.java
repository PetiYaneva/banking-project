package com.example.banking_project.account.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.web.dto.CreateAccountRequest;
import com.example.banking_project.web.dto.TransferRequest;
import com.example.banking_project.web.dto.TransferResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface AccountService {
    Account create(CreateAccountRequest request, UUID userId);
    List<Account> getAllAccounts();

    Optional<Account> getAccountById(UUID id);
    Optional<Account> getAccountByIban(String iban);

    Account updateBalance(UUID accountId, BigDecimal newBalance);
    void deleteAccount(UUID accountId);

    TransferResponse transfer(TransferRequest request);

    // Допълнително:
    List<Account> getAccountsByUserId(UUID userId);
    BigDecimal getTotalBalanceForUser(UUID userId);
}
