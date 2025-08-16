package com.example.banking_project.web.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class LoanRiskReportData {
    private UUID userId;
    private BigDecimal declaredIncome;
    private BigDecimal calculatedMonthlyIncome;
    private BigDecimal calculatedMonthlyExpenses;
    private BigDecimal availableIncome;
    private BigDecimal obligations;
    private BigDecimal dti;
    private String creditHistory;
    private int score;
    private String riskClass;
    private String recommendation;
    private LocalDate generatedOn;
}


