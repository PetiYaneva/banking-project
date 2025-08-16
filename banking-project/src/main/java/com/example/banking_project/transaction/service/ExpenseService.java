package com.example.banking_project.transaction.service;

import com.example.banking_project.web.dto.ExpenseRequest;

import java.math.BigDecimal;
import java.util.UUID;

public interface ExpenseService {
    void createExpense(ExpenseRequest request);

    BigDecimal getExpensesForLastMonths(UUID userId, int monthsBack);
}
