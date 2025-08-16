package com.example.banking_project.transaction.service;

import com.example.banking_project.transaction.model.Expense;
import com.example.banking_project.transaction.repository.ExpenseRepository;
import com.example.banking_project.web.dto.ExpenseRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public void createExpense(ExpenseRequest request) {
        Expense expense = expenseRepository.save(Expense.builder()
                .amount(request.getAmount())
                .transaction(request.getTransaction())
                .build());
        log.info("Saving expense: {} for transaction: {}", expense.getId(), request.getTransaction().getId());
    }

    @Override
    public BigDecimal getExpensesForLastMonths(UUID userId, int monthsBack) {
        LocalDate startDate = LocalDate.now().minusMonths(monthsBack);
        return expenseRepository.getExpensesForLastMonths(userId, startDate);
    }
}
