package com.example.banking_project.web.controllers;

import com.example.banking_project.transaction.model.TransactionType;
import com.example.banking_project.transaction.service.ExpenseService;
import com.example.banking_project.transaction.service.IncomeService;
import com.example.banking_project.transaction.service.TransactionService;
import com.example.banking_project.web.dto.TransactionRequest;
import com.example.banking_project.web.dto.TransactionResponse;
import com.example.banking_project.web.dto.TransactionTransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @PostMapping("/new")
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionTransferResponse> getById(@PathVariable UUID id) {
        TransactionTransferResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionTransferResponse>> getByUser(@PathVariable UUID userId) {
        List<TransactionTransferResponse> transactions = transactionService.getAllTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/type")
    public ResponseEntity<List<TransactionTransferResponse>> getByType(@RequestParam UUID userId,
                                                                       @RequestParam TransactionType type) {
        List<TransactionTransferResponse> transactions = transactionService.getTransactionsByType(userId, type);
        return ResponseEntity.ok(transactions);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/period")
    public ResponseEntity<List<TransactionTransferResponse>> getByUserAndPeriod(@RequestParam UUID userId,
                                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TransactionTransferResponse> transactions =
                transactionService.getTransactionsByUserAndPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/income/summary")
    public ResponseEntity<BigDecimal> getIncomeSummary(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "6") int monthsBack) {
        BigDecimal income = incomeService.getIncomeForLastMonths(userId, monthsBack);
        return ResponseEntity.ok(income);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/expense/summary")
    public ResponseEntity<BigDecimal> getExpenseSummary(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "6") int monthsBack) {
        BigDecimal expense = expenseService.getExpensesForLastMonths(userId, monthsBack);
        return ResponseEntity.ok(expense);
    }
}