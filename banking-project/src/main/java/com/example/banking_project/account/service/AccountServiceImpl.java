package com.example.banking_project.account.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.repository.AccountRepository;
import com.example.banking_project.account.validation.AccountValidationService;
import com.example.banking_project.transaction.model.TransactionType;
import com.example.banking_project.transaction.service.TransactionService;
import com.example.banking_project.user.model.User;
import com.example.banking_project.web.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{
    private final AccountRepository accountRepository;
    private final AccountValidationService accountValidationService;
    private final TransactionService transactionService;

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
    public Account getAccountById(UUID id) {
        return accountRepository.findAccountById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
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
    public TransferResponse transfer(TransferRequest request) {
        accountValidationService.validateAccountExistsByIban(request.getSenderIban());
        accountValidationService.validateAccountExistsByIban(request.getReceiverIban());

        Account sender = accountRepository.findAccountByIban(request.getSenderIban())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

        Account receiver = accountRepository.findAccountByIban(request.getReceiverIban())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        accountValidationService.validateTransferRequest(sender.getId(), receiver.getId(), request.getAmount());

        updateBalance(sender.getId(), sender.getBalance().subtract(request.getAmount()));
        updateBalance(receiver.getId(), receiver.getBalance().add(request.getAmount()));

        TransactionRequest transactionRequestSender = buildTransactionRequest(
                request, sender, sender.getUser().getId(), false, true);

        TransactionRequest transactionRequestReceiver = buildTransactionRequest(
                request, receiver, receiver.getUser().getId(), true, false);

        TransactionResponse transactionReceiver = transactionService.createTransaction(transactionRequestReceiver);
        TransactionResponse transactionSender = transactionService.createTransaction(transactionRequestSender);

        return TransferResponse.builder()
                .senderIban(sender.getIban())
                .receiverIban(receiver.getIban())
                .amount(transactionReceiver.getAmount())
                .currency(transactionReceiver.getCurrency().getCurrencyCode())
                .description(transactionReceiver.getDescription())
                .status(transactionReceiver.getTransactionStatus().toString())
                .createdOn(transactionReceiver.getCreatedOn())
                .transactionId(transactionSender.getId().toString())
                .build();
    }

    private TransactionRequest buildTransactionRequest(TransferRequest transferRequest, Account account, UUID userId,
                                                       Boolean isIncome, Boolean isExpense) {
        return TransactionRequest.builder()
                .account(account)
                .transactionType(TransactionType.TRANSFER)
                .amount(transferRequest.getAmount())
                .currency(Currency.getInstance(transferRequest.getCurrency()))
                .description(transferRequest.getDescription())
                .isExpense(isExpense)
                .isIncome(isIncome)
                .userId(userId)
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
