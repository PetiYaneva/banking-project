package com.example.banking_project.account.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.repository.AccountRepository;
import com.example.banking_project.account.validation.AccountValidationService;
import com.example.banking_project.exception.BusinessRuleViolationException;
import com.example.banking_project.transaction.model.Transaction;
import com.example.banking_project.transaction.model.TransactionStatus;
import com.example.banking_project.transaction.model.TransactionType;
import com.example.banking_project.transaction.repository.TransactionRepository;
import com.example.banking_project.user.model.User;
import com.example.banking_project.web.dto.CreateAccountRequest;
import com.example.banking_project.web.dto.TransferResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{
    private final AccountRepository accountRepository;
    private final AccountValidationService accountValidationService;
    private final TransactionRepository transactionRepository;

    @Override
    public Account create(CreateAccountRequest request, UUID userId) {
        String iban = createIban();

        while (accountRepository.existsByIban(iban)){
            iban = createIban();
        }
        Account account = Account.builder()
                .iban(iban)
                .accountType(request.getAccountType())
                .balance(request.getInitialBalance())
                .user(User.builder().id(userId).build()) // само ID
                .build();
        return accountRepository.save(account);

    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> getAccountById(UUID id) {
        return accountRepository.findAccountById(id);
    }

    @Override
    public Optional<Account> getAccountByIban(String iban) {
        return accountRepository.findAccountByIban(iban);
    }

    @Override
    public Account updateBalance(UUID accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        account.setBalance(newBalance);
        return accountRepository.save(account);
    }

    @Override
    public void deleteAccount(UUID accountId) {
        accountValidationService.validateAccountExistsById(accountId);
        accountRepository.deleteById(accountId);
    }

    @Transactional
    @Override
    public TransferResponse transfer(String senderIban, String receiverIban, BigDecimal amount, String description) {
        accountValidationService.validateAccountExistsByIban(senderIban);
        accountValidationService.validateAccountExistsByIban(receiverIban);

        Account sender = accountRepository.findAccountByIban(senderIban)
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

        Account receiver = accountRepository.findAccountByIban(receiverIban)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        if (sender.getIban().equals(receiver.getIban())) {
            throw new BusinessRuleViolationException("Sender and receiver cannot be the same account.");
        }

        accountValidationService.validateSufficientBalance(sender.getId(), amount);

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = Transaction.builder()
                .sender(senderIban)
                .receiver(receiverIban)
                .transactionStatus(TransactionStatus.SUCCEEDED)
                .amount(amount)
                .currency(Currency.getInstance("BGN"))
                .transactionType(TransactionType.DEPOSIT)
                .description(description)
                .createdOn(LocalDate.now())
                .account(sender)
                .user(sender.getUser())
                .build();

         transactionRepository.save(transaction);
        return TransferResponse.builder()
                .senderIban(transaction.getSender())
                .receiverIban(transaction.getReceiver())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency().getCurrencyCode())
                .description(transaction.getDescription())
                .status(transaction.getTransactionStatus().name())
                .createdOn(transaction.getCreatedOn())
                .transactionId(transaction.getId().toString())
                .build();
    }


    @Override
    public List<Account> getAccountsByUserId(UUID userId) {
        return accountRepository.findAllAccountsByUserId(userId);
    }

    @Override
    public BigDecimal getTotalBalanceForUser(UUID userId) {
        List<Account> accounts = accountRepository.findAllAccountsByUserId(userId);
        return accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String createIban() {
        String title ="BG25BPTU";

        StringBuilder iban = new StringBuilder(title);
        Random random = new Random();

        for (int i = 0; i < 22 - title.length(); i++) {
            iban.append(random.nextInt(10));
        }

        return iban.toString();
    }
}
