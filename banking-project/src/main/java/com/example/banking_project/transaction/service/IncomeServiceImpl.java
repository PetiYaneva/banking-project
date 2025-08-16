package com.example.banking_project.transaction.service;

import com.example.banking_project.transaction.model.Expense;
import com.example.banking_project.transaction.model.Income;
import com.example.banking_project.transaction.repository.IncomeRepository;
import com.example.banking_project.web.dto.IncomeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;

    @Override
    public void createIncome(IncomeRequest request) {
        Income income = incomeRepository.save(Income.builder()
                .amount(request.getAmount())
                .transaction(request.getTransaction())
                .build());
        log.info("Saving expense: {} for transaction: {}", income.getId(), request.getTransaction().getId());

    }
    @Override
    public BigDecimal getIncomeForLastMonths(UUID userId, int monthsBack) {
        LocalDate startDate = LocalDate.now().minusMonths(monthsBack);
        return incomeRepository.getIncomeForLastMonths(userId, startDate);
    }
}
