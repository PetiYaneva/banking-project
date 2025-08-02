package com.example.banking_project.transaction.service;

import com.example.banking_project.web.dto.ExpenseRequest;

public interface ExpenseService {
    void createExpense(ExpenseRequest request);
}
