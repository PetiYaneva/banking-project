package com.example.banking_project.account.validation;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.repository.AccountRepository;
import com.example.banking_project.exception.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountValidationServiceImpl implements AccountValidationService {

    private final AccountRepository accountRepository;

    @Override
    public void validateAccountExistsById(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account with ID [%d] does not exist.".formatted(accountId));
        }
    }

    @Override
    public void validateAccountExistsByIban(String iban) {
        if (!accountRepository.existsByIban(iban)) {
            throw new ResourceNotFoundException("Account with IBAN [%s] does not exist.".formatted(iban));
        }
    }

    @Override
    public void validateOwnership(UUID accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUser().getId().equals(userId)) {
            throw new BusinessRuleViolationException("User does not own this account.");
        }
    }

    @Override
    public void validateSufficientBalance(UUID accountId, BigDecimal amount) {
        Account acc = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleViolationException("Insufficient funds.");
        }
    }
}