package com.example.banking_project.account.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.model.AccountType;
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
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountValidationService accountValidationService;
    private final TransactionService transactionService;

    @Override
    public Account create(CreateAccountRequest request, UUID userId) {
        String iban = createIban();
        while (accountRepository.existsByIban(iban)) {
            iban = createIban();
        }

        String currencyCode = Optional.ofNullable(request.getCurrencyCode())
                .map(String::trim)
                .filter(s -> !((String) s).isEmpty())
                .orElse("BGN");

        Account account = Account.builder()
                .iban(iban)
                .accountType(request.getAccountType())
                .balance(request.getInitialBalance())
                .currencyCode(currencyCode)
                .cryptoEnabled(Boolean.TRUE)
                .user(User.builder().id(userId).build())
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
    public Account getAccountByIban(String iban) {
        return accountRepository.findAccountByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
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

        TransactionTransferRequest transactionRequestSender = buildTransactionRequest(
                request, sender, sender.getUser().getId(), false, true);

        TransactionTransferRequest transactionRequestReceiver = buildTransactionRequest(
                request, receiver, receiver.getUser().getId(), true, false);

        TransactionTransferResponse transactionReceiver =
                transactionService.createTransactionTransfer(transactionRequestReceiver);
        TransactionTransferResponse transactionSender =
                transactionService.createTransactionTransfer(transactionRequestSender);

        return TransferResponse.builder()
                .senderIban(sender.getIban())
                .receiverIban(receiver.getIban())
                .amount(transactionReceiver.getAmount())
                .currency(transactionReceiver.getCurrency().getCurrencyCode().toString())
                .description(transactionReceiver.getDescription())
                .status(transactionReceiver.getTransactionStatus().toString())
                .createdOn(transactionReceiver.getCreatedOn())
                .transactionId(transactionSender.getId().toString())
                .build();
    }

    private TransactionTransferRequest buildTransactionRequest(TransferRequest transferRequest,
                                                               Account account,
                                                               UUID userId,
                                                               Boolean isIncome,
                                                               Boolean isExpense) {
        return TransactionTransferRequest.builder()
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

    @Override
    public void createCreditAccount(LoanRequest request, UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to create a credit account.");
        }
        BigDecimal principal = request != null && request.getTotalAmount() != null
                ? request.getTotalAmount()
                : BigDecimal.ZERO;

        String iban = createIban();
        while (accountRepository.existsByIban(iban)) {
            iban = createIban();
        }

        String currencyCode = "BGN";

        Account creditAccount = Account.builder()
                .iban(iban)
                .accountType(AccountType.CREDIT)
                .balance(principal)
                .currencyCode(currencyCode)
                .cryptoEnabled(Boolean.TRUE)
                .user(User.builder().id(userId).build())
                .build();

        accountRepository.save(creditAccount);
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

    @Transactional
    @Override
    public Account debitByIban(String iban, BigDecimal amount) {
        Account acc = accountRepository.findAccountByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        acc.setBalance(acc.getBalance().subtract(amount));
        return accountRepository.save(acc); // @Version в Account пази от race conditions
    }

    @Transactional
    @Override
    public Account creditByIban(String iban, BigDecimal amount) {
        Account acc = accountRepository.findAccountByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        acc.setBalance(acc.getBalance().add(amount));
        return accountRepository.save(acc);
    }
}
