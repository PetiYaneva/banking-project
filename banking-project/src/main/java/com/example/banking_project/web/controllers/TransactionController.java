package com.example.banking_project.web.controllers;

import com.example.banking_project.transaction.model.TransactionType;
import com.example.banking_project.transaction.service.TransactionService;
import com.example.banking_project.web.dto.TransactionRequest;
import com.example.banking_project.web.dto.TransactionResponse;
import com.example.banking_project.web.dto.TransactionTransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // Създаване на трансакция
    @PostMapping("/new")
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.ok(response);
    }

    // Трансакция по ID
    @GetMapping("/{id}")
    public ResponseEntity<TransactionTransferResponse> getById(@PathVariable UUID id) {
        TransactionTransferResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    // Всички трансакции на потребител
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionTransferResponse>> getByUser(@PathVariable UUID userId) {
        List<TransactionTransferResponse> transactions = transactionService.getAllTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    // По тип трансакция
    @GetMapping("/type")
    public ResponseEntity<List<TransactionTransferResponse>> getByType(@RequestParam UUID userId,
                                                                       @RequestParam TransactionType type) {
        List<TransactionTransferResponse> transactions = transactionService.getTransactionsByType(userId, type);
        return ResponseEntity.ok(transactions);
    }

    // По период от време
    @GetMapping("/period")
    public ResponseEntity<List<TransactionTransferResponse>> getByUserAndPeriod(@RequestParam UUID userId,
                                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TransactionTransferResponse> transactions = transactionService.getTransactionsByUserAndPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
}

