package com.example.banking_project.transaction.service;

import com.example.banking_project.account.service.AccountService;
import com.example.banking_project.transaction.model.Transaction;
import com.example.banking_project.transaction.model.TransactionStatus;
import com.example.banking_project.transaction.model.TransactionType;
import com.example.banking_project.transaction.repository.TransactionRepository;
import com.example.banking_project.user.model.User;
import com.example.banking_project.user.repository.UserRepository;
import com.example.banking_project.user.service.UserService;
import com.example.banking_project.web.dto.ExpenseRequest;
import com.example.banking_project.web.dto.IncomeRequest;
import com.example.banking_project.web.dto.TransactionRequest;
import com.example.banking_project.web.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final UserService userService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {
      User user = userService.findUserById(request.getUserId());

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .createdOn(LocalDate.now())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .transactionStatus(TransactionStatus.SUCCEEDED)
                .account(accountService.getAccountById(request.getAccountId()))
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

        return TransactionResponse.builder()
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
                .build();
    }
}
