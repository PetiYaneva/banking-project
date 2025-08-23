package com.example.banking_project.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LoanRiskResult {
    private int score;
    private String riskClass;
    private String recommendation;

    private BigDecimal averageNetIncome;
    private BigDecimal declaredIncome;
    private BigDecimal averageIncome;
    private BigDecimal totalExpenses;
    private BigDecimal dti;

    private UUID createdLoanId;
    private BigDecimal monthlyPayment;
}
