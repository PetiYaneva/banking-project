package com.example.banking_project.account.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.repository.AccountRepository;
import com.example.banking_project.exception.BusinessRuleViolationException;
import com.example.banking_project.transaction.model.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountBalanceServiceImpl implements AccountBalanceService{

    private final AccountRepository accountRepository;

    @Override
    public void updateBalance(String iban, BigDecimal amount, TransactionType type) {
        Account account = accountRepository.findAccountByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (type == TransactionType.WITHDRAWAL || type == TransactionType.WITHDRAWAL) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new BusinessRuleViolationException("Insufficient balance.");
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else if (type == TransactionType.DEPOSIT) {
            account.setBalance(account.getBalance().add(amount));
        }

        accountRepository.save(account);
    }

    @Override
    public Account getAccountByIban(String iban) {
        return accountRepository.findAccountByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Account with IBAN " + iban + " not found."));
    }
}
