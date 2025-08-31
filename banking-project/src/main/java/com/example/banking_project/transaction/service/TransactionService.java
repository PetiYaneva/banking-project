package com.example.banking_project.transaction.service;

import com.example.banking_project.transaction.model.TransactionType;
import com.example.banking_project.web.dto.TransactionRequest;
import com.example.banking_project.web.dto.TransactionResponse;
import com.example.banking_project.web.dto.TransactionTransferRequest;
import com.example.banking_project.web.dto.TransactionTransferResponse;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionService {
    TransactionTransferResponse createTransactionTransfer(TransactionTransferRequest request);
    TransactionResponse createTransaction(TransactionRequest request);

    List<TransactionTransferResponse> getAllTransactions();
    TransactionTransferResponse getTransactionById(UUID transactionId);

    List<TransactionTransferResponse> getAllIncomesByUser(UUID userId);
    List<TransactionTransferResponse> getAllExpensesByUser(UUID userId);
    List<TransactionTransferResponse> getTransactionsByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate);
    List<TransactionTransferResponse> getIncomesByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate);
    List<TransactionTransferResponse> getExpensesByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate);
    List<TransactionTransferResponse> getTransactionsByAccount(UUID accountId);
    List<TransactionTransferResponse> getTransactionsByType(UUID userId, TransactionType type);
    List<TransactionTransferResponse> getAllTransactionsByUserId(UUID userId);

    void recordExpense(UUID userId, String iban, BigDecimal amount, String description);
    void recordIncome(UUID userId, String iban, BigDecimal amount, String description);

    List<TransactionTransferResponse> getAllByPeriod(LocalDate startDate, LocalDate endDate);
    List<TransactionTransferResponse> getAllByPeriodAndType(LocalDate startDate, LocalDate endDate, TransactionType type);
    List<TransactionTransferResponse> getAllByPeriodAndStatus(LocalDate startDate, LocalDate endDate, String status);
    List<TransactionTransferResponse> getAllByPeriodAndCurrency(LocalDate startDate, LocalDate endDate, String currency);
    List<TransactionTransferResponse> getByAccountAndPeriod(UUID accountId, LocalDate startDate, LocalDate endDate);
    List<TransactionTransferResponse> getAllByPeriodAndMinAmount(LocalDate startDate, LocalDate endDate, BigDecimal minAmount);
    List<TransactionTransferResponse> getAllByPeriodAndAmountBetween(LocalDate startDate, LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount);
    List<TransactionTransferResponse> searchAllByDescriptionInPeriod(LocalDate startDate, LocalDate endDate, String query);

    BigDecimal sumIncomeByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate);
    BigDecimal sumExpenseByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate);
    BigDecimal netCashFlowByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate);
    BigDecimal sumIncomeByAccountAndPeriod(UUID accountId, LocalDate startDate, LocalDate endDate);
    BigDecimal sumExpenseByAccountAndPeriod(UUID accountId, LocalDate startDate, LocalDate endDate);
}
