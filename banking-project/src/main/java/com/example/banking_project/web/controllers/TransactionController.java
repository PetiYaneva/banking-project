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
        return ResponseEntity.ok(transactionService.createTransaction(request));
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionTransferResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionTransferResponse>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(transactionService.getAllTransactionsByUserId(userId));
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/type")
    public ResponseEntity<List<TransactionTransferResponse>> getByType(@RequestParam UUID userId,
                                                                       @RequestParam TransactionType type) {
        return ResponseEntity.ok(transactionService.getTransactionsByType(userId, type));
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/period")
    public ResponseEntity<List<TransactionTransferResponse>> getByUserAndPeriod(@RequestParam UUID userId,
                                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByUserAndPeriod(userId, startDate, endDate));
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/income/summary")
    public ResponseEntity<BigDecimal> getIncomeSummary(@RequestParam UUID userId,
                                                       @RequestParam(defaultValue = "6") int monthsBack) {
        return ResponseEntity.ok(incomeService.getIncomeForLastMonths(userId, monthsBack));
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    @GetMapping("/expense/summary")
    public ResponseEntity<BigDecimal> getExpenseSummary(@RequestParam UUID userId,
                                                        @RequestParam(defaultValue = "6") int monthsBack) {
        return ResponseEntity.ok(expenseService.getExpensesForLastMonths(userId, monthsBack));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<TransactionTransferResponse>> getAll() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/period")
    public ResponseEntity<List<TransactionTransferResponse>> getAllByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getAllByPeriod(startDate, endDate));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/period/type")
    public ResponseEntity<List<TransactionTransferResponse>> getAllByPeriodAndType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam TransactionType type) {
        return ResponseEntity.ok(transactionService.getAllByPeriodAndType(startDate, endDate, type));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/period/status")
    public ResponseEntity<List<TransactionTransferResponse>> getAllByPeriodAndStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String status) {
        return ResponseEntity.ok(transactionService.getAllByPeriodAndStatus(startDate, endDate, status));
    }

    // По период и валута
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/period/currency")
    public ResponseEntity<List<TransactionTransferResponse>> getAllByPeriodAndCurrency(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String currency) {
        return ResponseEntity.ok(transactionService.getAllByPeriodAndCurrency(startDate, endDate, currency));
    }

    // По акаунт и период
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/account/{accountId}/period")
    public ResponseEntity<List<TransactionTransferResponse>> getByAccountAndPeriod(
            @PathVariable UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getByAccountAndPeriod(accountId, startDate, endDate));
    }

    // По период и минимална сума
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/period/min-amount")
    public ResponseEntity<List<TransactionTransferResponse>> getAllByPeriodAndMinAmount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam BigDecimal minAmount) {
        return ResponseEntity.ok(transactionService.getAllByPeriodAndMinAmount(startDate, endDate, minAmount));
    }

    // По период и интервал от суми
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/period/amount-between")
    public ResponseEntity<List<TransactionTransferResponse>> getAllByPeriodAndAmountBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {
        return ResponseEntity.ok(transactionService.getAllByPeriodAndAmountBetween(startDate, endDate, minAmount, maxAmount));
    }

    // Търсене по описание (full-text lite) в период
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/period/search")
    public ResponseEntity<List<TransactionTransferResponse>> searchAllByDescriptionInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(transactionService.searchAllByDescriptionInPeriod(startDate, endDate, q));
    }

    // ===== ADMIN aggregates =====

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/aggregate/user/income")
    public ResponseEntity<BigDecimal> sumIncomeByUserAndPeriod(
            @RequestParam UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.sumIncomeByUserAndPeriod(userId, startDate, endDate));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/aggregate/user/expense")
    public ResponseEntity<BigDecimal> sumExpenseByUserAndPeriod(
            @RequestParam UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.sumExpenseByUserAndPeriod(userId, startDate, endDate));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/aggregate/user/net-cashflow")
    public ResponseEntity<BigDecimal> netCashFlowByUserAndPeriod(
            @RequestParam UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.netCashFlowByUserAndPeriod(userId, startDate, endDate));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/aggregate/account/income")
    public ResponseEntity<BigDecimal> sumIncomeByAccountAndPeriod(
            @RequestParam UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.sumIncomeByAccountAndPeriod(accountId, startDate, endDate));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/aggregate/account/expense")
    public ResponseEntity<BigDecimal> sumExpenseByAccountAndPeriod(
            @RequestParam UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.sumExpenseByAccountAndPeriod(accountId, startDate, endDate));
    }
}
