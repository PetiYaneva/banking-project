package com.example.banking_project.transaction.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.service.AccountBalanceService;
import com.example.banking_project.account.service.AccountService;
import com.example.banking_project.transaction.model.Transaction;
import com.example.banking_project.transaction.model.TransactionStatus;
import com.example.banking_project.transaction.model.TransactionType;
import com.example.banking_project.transaction.repository.TransactionRepository;
import com.example.banking_project.user.model.User;
import com.example.banking_project.user.service.UserService;
import com.example.banking_project.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final AccountBalanceService accountBalanceService;

    @Override
    public TransactionTransferResponse createTransactionTransfer(TransactionTransferRequest request) {
      User user = userService.findUserById(request.getUserId());

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .createdOn(LocalDate.now())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .transactionStatus(TransactionStatus.SUCCEEDED)
                .account(request.getAccount())
                .isExpense(request.getIsExpense())
                .isIncome(request.getIsIncome())
                .transactionType(request.getTransactionType())
                .user(user)
                .build();
        transactionRepository.save(transaction);

        if (transaction.getIsIncome()) {
            incomeService.createIncome(IncomeRequest.builder()
                    .amount(transaction.getAmount())
                    .transaction(transaction)
                    .build());
        } else {
            expenseService.createExpense(ExpenseRequest.builder()
                    .amount(transaction.getAmount())
                    .transaction(transaction)
                    .build());
        }

        return TransactionTransferResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionStatus(transaction.getTransactionStatus().toString())
                .transactionType(transaction.getTransactionType().toString())
                .description(transaction.getDescription())
                .accountId(transaction.getAccount().getId())
                .userId(transaction.getUser().getId())
                .isExpense(transaction.getIsExpense())
                .isIncome(transaction.getIsIncome())
                .createdOn(transaction.getCreatedOn())
                .build();
    }

    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {
        Account account = accountBalanceService.getAccountByIban(request.getIban());
        User user = userService.findUserById(request.getUserId());

        // Обновяване на баланса според типа
        accountBalanceService.updateBalance(
                request.getIban(),
                request.getAmount(),
                request.getTransactionType()
        );

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .createdOn(LocalDate.now())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .transactionStatus(TransactionStatus.SUCCEEDED)
                .account(account)
                .user(user)
                .isIncome(request.getTransactionType() == TransactionType.DEPOSIT)
                .isExpense(request.getTransactionType() == TransactionType.WITHDRAWAL)
                .transactionType(request.getTransactionType())
                .build();

        transactionRepository.save(transaction);

        if (transaction.getIsIncome()) {
            incomeService.createIncome(IncomeRequest.builder()
                    .amount(transaction.getAmount())
                    .transaction(transaction)
                    .build());
        } else {
            expenseService.createExpense(ExpenseRequest.builder()
                    .amount(transaction.getAmount())
                    .transaction(transaction)
                    .build());
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionStatus(transaction.getTransactionStatus().toString())
                .transactionType(transaction.getTransactionType().toString())
                .description(transaction.getDescription())
                .userId(transaction.getUser().getId())
                .isExpense(transaction.getIsExpense())
                .isIncome(transaction.getIsIncome())
                .createdOn(transaction.getCreatedOn())
                .build();
    }


    //Помисли дали ти трябва!!
    @Override
    public List<TransactionTransferResponse> getAllTransactions() {
        return null;
    }

    @Override
    public TransactionTransferResponse getTransactionById(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return TransactionTransferResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .transactionStatus(transaction.getTransactionStatus().toString())
                .transactionType(transaction.getTransactionType().toString())
                .description(transaction.getDescription())
                .failureReason(transaction.getFailureReason() != null ? transaction.getFailureReason() : "")
                .createdOn(transaction.getCreatedOn())
                .currency(transaction.getCurrency())
                .isIncome(transaction.getIsIncome())
                .isExpense(transaction.getIsExpense())
                .accountId(transaction.getAccount().getId())
                .userId(transaction.getUser().getId())
                .build();
    }

    @Override
    public List<TransactionTransferResponse> getAllIncomesByUser(UUID userId) {
        return transactionRepository.getAllIncomesByUserId(userId);
    }

    @Override
    public List<TransactionTransferResponse> getAllExpensesByUser(UUID userId) {
        return transactionRepository.getAllExpensesByUserId(userId);
    }

    @Override
    public List<TransactionTransferResponse> getTransactionsByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTransactionsByUserAndPeriod(userId, startDate, endDate);
    }

    @Override
    public List<TransactionTransferResponse> getIncomesByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getIncomesByUserAndPeriod(userId, startDate, endDate);
    }

    @Override
    public List<TransactionTransferResponse> getExpensesByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getExpensesByUserAndPeriod(userId, startDate, endDate);
    }

    @Override
    public List<TransactionTransferResponse> getTransactionsByAccount(UUID accountId) {
        return transactionRepository.getTransactionsByAccount(accountId);
    }

    @Override
    public List<TransactionTransferResponse> getTransactionsByType(UUID userId, TransactionType type) {
        return transactionRepository.findByUserIdAndType(userId, type.toString());
    }

    @Override
    public List<TransactionTransferResponse> getAllTransactionsByUserId(UUID userId) {
        return transactionRepository.getAllTransactionsByUserId(userId);
    }

    private TransactionTransferResponse mapToResponse(Transaction t) {
        return TransactionTransferResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .transactionStatus(t.getTransactionStatus().toString())
                .transactionType(t.getTransactionType().toString())
                .description(t.getDescription())
                .failureReason(t.getFailureReason())
                .createdOn(t.getCreatedOn())
                .accountId(t.getAccount().getId())
                .userId(t.getUser().getId())
                .isIncome(t.getIsIncome())
                .isExpense(t.getIsExpense())
                .build();
    }
}
