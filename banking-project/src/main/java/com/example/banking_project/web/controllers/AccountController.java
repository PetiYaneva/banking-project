package com.example.banking_project.web.controllers;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.service.AccountService;
import com.example.banking_project.web.dto.CreateAccountRequest;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/users/me/accounts")
    public ResponseEntity<List<Account>> getUserAccounts(HttpServletRequest request) {
        UUID userId = UUID.fromString((String) request.getAttribute("userId"));
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @GetMapping("/users/me/accounts/balance")
    public ResponseEntity<BigDecimal> getUserTotalBalance(HttpServletRequest request) {
        UUID userId = UUID.fromString((String) request.getAttribute("userId"));
        return ResponseEntity.ok(accountService.getTotalBalanceForUser(userId));
    }

    @PostMapping("/users/me/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest requestBody,
                                                 HttpServletRequest request) {
        UUID userId = UUID.fromString((String) request.getAttribute("userId"));
        return ResponseEntity.ok(accountService.create(requestBody, userId));
    }

    @PostMapping("/accounts/transfer")
    public ResponseEntity<TransferResponse> transferMoney(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(accountService.transfer(request));
    }

    @GetMapping("/accounts/{iban}")
    public ResponseEntity<Account> getAccountByIban(@PathVariable String iban) {
        return accountService.getAccountByIban(iban)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

