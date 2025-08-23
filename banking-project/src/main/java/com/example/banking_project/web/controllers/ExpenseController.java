package com.example.banking_project.web.controllers;

import com.example.banking_project.transaction.service.TransactionService;
import com.example.banking_project.web.dto.TransactionTransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final TransactionService transactionService;

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionTransferResponse>> getExpensesByUser(@PathVariable UUID userId) {
        List<TransactionTransferResponse> expenses = transactionService.getAllExpensesByUser(userId);
        return ResponseEntity.ok(expenses);
    }

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping("/user/{userId}/period")
    public ResponseEntity<List<TransactionTransferResponse>> getExpensesByUserAndPeriod(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        List<TransactionTransferResponse> expenses =
                transactionService.getExpensesByUserAndPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(expenses);
    }
}
