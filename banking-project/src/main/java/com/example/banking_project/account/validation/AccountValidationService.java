package com.example.banking_project.account.validation;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public interface AccountValidationService {
    void validateAccountExistsById(UUID accountId);
    void validateAccountExistsByIban(String iban);
    void validateOwnership(UUID accountId, Long userId);
    void validateSufficientBalance(UUID accountId, BigDecimal amount);
    void validateTransferRequest(UUID senderAccountId, UUID receiverAccountId, BigDecimal amount);
}

