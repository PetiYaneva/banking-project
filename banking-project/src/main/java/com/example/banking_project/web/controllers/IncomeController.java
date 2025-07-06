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
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final TransactionService transactionService;

    // Всички приходи за потребител
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionTransferResponse>> getIncomesByUser(@RequestParam UUID userId) {
        List<TransactionTransferResponse> incomes = transactionService.getAllIncomesByUser(userId);
        return ResponseEntity.ok(incomes);
    }

    //Приходи за потребител за определен период
    @GetMapping("/user/{userId}/period")
    public ResponseEntity<List<TransactionTransferResponse>> getIncomesByUserAndPeriod(
            @RequestParam UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<TransactionTransferResponse> incomes = transactionService.getIncomesByUserAndPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(incomes);
    }
}
