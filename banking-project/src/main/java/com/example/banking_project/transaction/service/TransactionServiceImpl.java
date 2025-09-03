package com.example.banking_project.transaction.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.service.AccountBalanceService;
import com.example.banking_project.transaction.mapper.TransactionMapper;
import com.example.banking_project.transaction.model.Transaction;
import com.example.banking_project.transaction.model.TransactionStatus;
import com.example.banking_project.transaction.model.TransactionType;
import com.example.banking_project.transaction.repository.TransactionRepository;
import com.example.banking_project.user.model.User;
import com.example.banking_project.user.service.UserService;
import com.example.banking_project.web.dto.ExpenseRequest;
import com.example.banking_project.web.dto.IncomeRequest;
import com.example.banking_project.web.dto.TransactionRequest;
import com.example.banking_project.web.dto.TransactionResponse;
import com.example.banking_project.web.dto.TransactionTransferRequest;
import com.example.banking_project.web.dto.TransactionTransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final AccountBalanceService accountBalanceService;
    private final TransactionMapper mapper;

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

        return mapper.fromEntity(transaction);
    }

    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {
        Account account = accountBalanceService.getAccountByIban(request.getIban());
        User user = userService.findUserById(request.getUserId());

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

    @Override
    public TransactionTransferResponse getTransactionById(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return mapper.fromEntity(transaction);
    }

    @Override
    public List<TransactionTransferResponse> getAllIncomesByUser(UUID userId) {
        return transactionRepository.getAllIncomesByUserId(userId).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getAllExpensesByUser(UUID userId) {
        return transactionRepository.getAllExpensesByUserId(userId).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getTransactionsByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTransactionsByUserAndPeriod(userId, startDate, endDate).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getIncomesByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getIncomesByUserAndPeriod(userId, startDate, endDate).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getExpensesByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getExpensesByUserAndPeriod(userId, startDate, endDate).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getTransactionsByAccount(UUID accountId) {
        return transactionRepository.getTransactionsByAccount(accountId).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getTransactionsByType(UUID userId, TransactionType type) {
        return transactionRepository.findByUserIdAndType(userId, type.toString()).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getAllTransactionsByUserId(UUID userId) {
        return transactionRepository.getAllTransactionsByUserId(userId).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(mapper::fromEntity)
                .toList();
    }

    @Override
    public void recordExpense(UUID userId, String iban, BigDecimal amount, String description) {
        var account = accountBalanceService.getAccountByIban(iban);

        accountBalanceService.updateBalance(iban, amount, TransactionType.WITHDRAWAL);

        Transaction tx = Transaction.builder()
                .amount(amount)
                .createdOn(LocalDate.now())
                .currency(Currency.getInstance(
                        account.getCurrencyCode() != null ? account.getCurrencyCode() : "BGN"))
                .description(description)
                .transactionStatus(TransactionStatus.SUCCEEDED)
                .account(account)
                .user(userService.findUserById(userId))
                .isExpense(true)
                .isIncome(false)
                .transactionType(TransactionType.WITHDRAWAL)
                .build();

        transactionRepository.save(tx);
        expenseService.createExpense(ExpenseRequest.builder()
                .amount(tx.getAmount())
                .transaction(tx)
                .build());
    }

    @Override
    public void recordIncome(UUID userId, String iban, BigDecimal amount, String description) {
        var account = accountBalanceService.getAccountByIban(iban);

        accountBalanceService.updateBalance(iban, amount, TransactionType.DEPOSIT);

        Transaction tx = Transaction.builder()
                .amount(amount)
                .createdOn(LocalDate.now())
                .currency(Currency.getInstance(
                        account.getCurrencyCode() != null ? account.getCurrencyCode() : "BGN"))
                .description(description)
                .transactionStatus(TransactionStatus.SUCCEEDED)
                .account(account)
                .user(userService.findUserById(userId))
                .isExpense(false)
                .isIncome(true)
                .transactionType(TransactionType.DEPOSIT)
                .build();

        transactionRepository.save(tx);
        incomeService.createIncome(IncomeRequest.builder()
                .amount(tx.getAmount())
                .transaction(tx)
                .build());
    }

    // ===== ADMIN списъци по период / филтри =====

    @Override
    public List<TransactionTransferResponse> getAllByPeriod(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getAllByPeriod(startDate, endDate).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getAllByPeriodAndType(LocalDate startDate, LocalDate endDate, TransactionType type) {
        return transactionRepository.getAllByPeriodAndType(startDate, endDate, type.toString()).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getAllByPeriodAndStatus(LocalDate startDate, LocalDate endDate, String status) {
        return transactionRepository.getAllByPeriodAndStatus(startDate, endDate, status).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getAllByPeriodAndCurrency(LocalDate startDate, LocalDate endDate, String currency) {
        return transactionRepository.getAllByPeriodAndCurrency(startDate, endDate, currency).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getByAccountAndPeriod(UUID accountId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getByAccountAndPeriod(accountId, startDate, endDate).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getAllByPeriodAndMinAmount(LocalDate startDate, LocalDate endDate, BigDecimal minAmount) {
        return transactionRepository.getAllByPeriodAndMinAmount(startDate, endDate, minAmount).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> getAllByPeriodAndAmountBetween(LocalDate startDate, LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount) {
        return transactionRepository.getAllByPeriodAndAmountBetween(startDate, endDate, minAmount, maxAmount).stream()
                .map(mapper::fromView)
                .toList();
    }

    @Override
    public List<TransactionTransferResponse> searchAllByDescriptionInPeriod(LocalDate startDate, LocalDate endDate, String query) {
        return transactionRepository.searchAllByDescriptionInPeriod(startDate, endDate, query).stream()
                .map(mapper::fromView)
                .toList();
    }

    // ===== ADMIN агрегати =====

    @Override
    public BigDecimal sumIncomeByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumIncomeByUserAndPeriod(userId, startDate, endDate);
    }

    @Override
    public BigDecimal sumExpenseByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumExpenseByUserAndPeriod(userId, startDate, endDate);
    }

    @Override
    public BigDecimal netCashFlowByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.netCashFlowByUserAndPeriod(userId, startDate, endDate);
    }

    @Override
    public BigDecimal sumIncomeByAccountAndPeriod(UUID accountId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumIncomeByAccountAndPeriod(accountId, startDate, endDate);
    }

    @Override
    public BigDecimal sumExpenseByAccountAndPeriod(UUID accountId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumExpenseByAccountAndPeriod(accountId, startDate, endDate);
    }
}
