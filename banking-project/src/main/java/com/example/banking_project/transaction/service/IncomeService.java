package com.example.banking_project.transaction.service;

import com.example.banking_project.web.dto.IncomeRequest;

import java.math.BigDecimal;
import java.util.UUID;

public interface IncomeService {
    void createIncome(IncomeRequest request);

    BigDecimal getIncomeForLastMonths(UUID userId, int monthsBack);
}
