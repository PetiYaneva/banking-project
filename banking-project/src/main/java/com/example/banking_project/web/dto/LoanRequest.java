package com.example.banking_project.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@Setter
public class LoanRequest {
    private UUID userId;
    private BigDecimal totalAmount;
    private BigDecimal interestRate;
    private LocalDate finalDate;
    private int termMonths;

    private BigDecimal monthlyIncome;
    private BigDecimal monthlyObligations;
    private int employmentYears;
    private String employmentType;
    private String creditHistory;
    private String collateral;
}

