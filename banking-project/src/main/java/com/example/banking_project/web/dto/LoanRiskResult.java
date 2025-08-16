package com.example.banking_project.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class LoanRiskResult {
    private int score;
    private String riskClass;
    private String recommendation;

    private BigDecimal averageNetIncome;
    private BigDecimal declaredIncome;
    private BigDecimal realIncome;
    private BigDecimal totalExpenses;
    private BigDecimal dti;
}
