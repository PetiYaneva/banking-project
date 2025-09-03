package com.example.banking_project.web.controllers;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.service.AccountService;
import com.example.banking_project.transaction.service.TransactionService;
import com.example.banking_project.web.dto.CreateAccountRequest;
import com.example.banking_project.web.dto.TransactionTransferResponse;
import com.example.banking_project.web.dto.TransferRequest;
import com.example.banking_project.web.dto.TransferResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping("/{userId}/accounts")
    public ResponseEntity<List<Account>> getUserAccounts(@PathVariable UUID userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getUserTotalBalance(@PathVariable UUID userId) {
        return ResponseEntity.ok(accountService.getTotalBalanceForUser(userId));
    }

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @PostMapping("/new")
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest requestBody,
                                                 HttpServletRequest request) {
        UUID userId = UUID.fromString((String) request.getAttribute("userId"));
        return ResponseEntity.ok(accountService.create(requestBody, userId));
    }

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transferMoney(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(accountService.transfer(request));
    }

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping("/{iban}")
    public Account getAccountByIban(@PathVariable String iban) {
        return ResponseEntity.ok(accountService.getAccountByIban(iban)).getBody();
    }

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionTransferResponse>> getByAccount(@PathVariable UUID id) {
        List<TransactionTransferResponse> transactions = transactionService.getTransactionsByAccount(id);
        return ResponseEntity.ok(transactions);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/high-balance")
    public ResponseEntity<List<Account>> getAccountsByBalanceGte(@RequestParam("min") BigDecimal min) {
        return ResponseEntity.ok(accountService.getAccountsByBalanceGte(min));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/low-balance")
    public ResponseEntity<List<Account>> getAccountsByBalanceLte(@RequestParam("max") BigDecimal max) {
        return ResponseEntity.ok(accountService.getAccountsByBalanceLte(max));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/currency/{currency}")
    public ResponseEntity<List<Account>> getAccountsByCurrency(@PathVariable String currency) {
        return ResponseEntity.ok(accountService.getAccountsByCurrency(currency));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/sorted/balance-desc")
    public ResponseEntity<List<Account>> getAllAccountsOrderByBalanceDesc() {
        return ResponseEntity.ok(accountService.getAllAccountsOrderByBalanceDesc());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/sorted/created-asc")
    public ResponseEntity<List<Account>> getAllAccountsOrderByCreatedOnAsc() {
        return ResponseEntity.ok(accountService.getAllAccountsOrderByCreatedOnAsc());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/id/{id}")
    public ResponseEntity<Account> getAccountByIdAdmin(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }
}
