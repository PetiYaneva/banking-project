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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping("/{userId}/accounts")
    public ResponseEntity<List<Account>> getUserAccounts(@PathVariable UUID userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getUserTotalBalance(@PathVariable UUID userId) {
        return ResponseEntity.ok(accountService.getTotalBalanceForUser(userId));
    }

    @PostMapping("/new")
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest requestBody,
                                                 HttpServletRequest request) {
        UUID userId = UUID.fromString((String) request.getAttribute("userId"));
        return ResponseEntity.ok(accountService.create(requestBody, userId));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transferMoney(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(accountService.transfer(request));
    }

    @GetMapping("/{iban}")
    public Account getAccountByIban(@PathVariable String iban) {
        return ResponseEntity.ok(accountService.getAccountByIban(iban)).getBody();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionTransferResponse>> getByAccount(@PathVariable UUID id) {
        List<TransactionTransferResponse> transactions = transactionService.getTransactionsByAccount(id);
        return ResponseEntity.ok(transactions);
    }
}

