package com.example.banking_project.web.controllers;

import com.example.banking_project.transaction.service.TransactionService;
import com.example.banking_project.web.dto.TransactionTransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final TransactionService transactionService;

    // Всички разходи за потребител
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionTransferResponse>> getExpensesByUser(@RequestParam UUID userId) {
        List<TransactionTransferResponse> expenses = transactionService.getAllExpensesByUser(userId);
        return ResponseEntity.ok(expenses);
    }

    // Разходи за потребител за определен период
    @GetMapping("/user/{userId}/period")
    public ResponseEntity<List<TransactionTransferResponse>> getExpensesByUserAndPeriod(
            @RequestParam UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<TransactionTransferResponse> expenses = transactionService.getExpensesByUserAndPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(expenses);
    }
}
