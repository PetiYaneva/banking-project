package com.example.banking_project.loan.service;

import com.example.banking_project.loan.repository.LoanRepository;
import com.example.banking_project.loan.view.CreditHistoryView;
import com.example.banking_project.transaction.service.ExpenseService;
import com.example.banking_project.transaction.service.IncomeService;
import com.example.banking_project.web.dto.LoanRequest;
import com.example.banking_project.web.dto.LoanRiskResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoanRiskEngine {

    private final LoanRepository loanRepository;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public LoanRiskResult assess(LoanRequest request) {
        final BigDecimal INC_1500 = new BigDecimal("1500");
        final BigDecimal INC_3000 = new BigDecimal("3000");
        final BigDecimal INC_5000 = new BigDecimal("5000");
        final BigDecimal DTI_30  = new BigDecimal("0.30");
        final BigDecimal DTI_50  = new BigDecimal("0.50");

        final int PTS_CREDIT_GOOD      = 20;
        final int PTS_CREDIT_NEUTRAL   = 10;

        final int PTS_DECL_GT_5000     = 20;
        final int PTS_DECL_3K_5K       = 15;
        final int PTS_DECL_1_5K_3K     = 10;
        final int PTS_DECL_LT_1_5K     = 5;

        final int PTS_DTI_LT_30        = 20;
        final int PTS_DTI_LE_50        = 10;

        final int PTS_EMP_FULL_TIME    = 10;
        final int PTS_EMP_PART_TIME    = 5;
        final int PTS_EMP_UNEMPLOYED   = -10;

        final int PEN_NO_REAL_INCOME_6M = -20;
        final int PEN_AVG_NET_LE_ZERO   = -15;
        final int PEN_AVG_NET_LT_50PCT  = -10;

        final int THRESH_LOW    = 55;
        final int THRESH_MEDIUM = 40;

        int score = 0;

        String creditHistory = creditHistoryEvaluation(request.getUserId());
        if ("good".equalsIgnoreCase(creditHistory)) {
            score += PTS_CREDIT_GOOD;
        } else if ("neutral".equalsIgnoreCase(creditHistory)) {
            score += PTS_CREDIT_NEUTRAL;
        }

        BigDecimal declaredIncome = nvl(request.getMonthlyIncome());
        if (declaredIncome.compareTo(INC_5000) > 0) {
            score += PTS_DECL_GT_5000;
        } else if (declaredIncome.compareTo(INC_3000) >= 0) {
            score += PTS_DECL_3K_5K;
        } else if (declaredIncome.compareTo(INC_1500) >= 0) {
            score += PTS_DECL_1_5K_3K;
        } else {
            score += PTS_DECL_LT_1_5K;
        }

        BigDecimal realIncome    = nvl(incomeService.getIncomeForLastMonths(request.getUserId(), 6));
        BigDecimal totalExpenses = nvl(expenseService.getExpensesForLastMonths(request.getUserId(), 6));
        if (realIncome.compareTo(BigDecimal.ZERO) == 0) {
            score += PEN_NO_REAL_INCOME_6M;
        }

        BigDecimal averageNetIncome = realIncome.subtract(totalExpenses)
                .divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);

        BigDecimal obligations = nvl(request.getMonthlyObligations());
        BigDecimal dti = BigDecimal.ZERO;
        if (declaredIncome.compareTo(BigDecimal.ZERO) > 0) {
            dti = obligations.divide(declaredIncome, 2, RoundingMode.HALF_UP);
        } else if (obligations.compareTo(BigDecimal.ZERO) > 0) {
            dti = BigDecimal.ONE;
        }

        if (dti.compareTo(DTI_30) < 0) {
            score += PTS_DTI_LT_30;
        } else if (dti.compareTo(DTI_50) <= 0) {
            score += PTS_DTI_LE_50;
        }

        String employmentType = request.getEmploymentType();
        if ("FULL_TIME".equalsIgnoreCase(employmentType)) {
            score += PTS_EMP_FULL_TIME;
        } else if ("PART_TIME".equalsIgnoreCase(employmentType)) {
            score += PTS_EMP_PART_TIME;
        } else if ("UNEMPLOYED".equalsIgnoreCase(employmentType)) {
            score += PTS_EMP_UNEMPLOYED;
        }

        if (averageNetIncome.compareTo(BigDecimal.ZERO) <= 0) {
            score += PEN_AVG_NET_LE_ZERO;
        } else if (averageNetIncome.compareTo(declaredIncome.multiply(new BigDecimal("0.5"))) < 0) {
            score += PEN_AVG_NET_LT_50PCT;
        }

        String riskClass;
        String recommendation;
        if (score >= THRESH_LOW) {
            riskClass = "Low Risk";
            recommendation = "Approved under standard conditions";
        } else if (score >= THRESH_MEDIUM) {
            riskClass = "Medium Risk";
            recommendation = "Approved with additional conditions";
        } else {
            riskClass = "High Risk";
            recommendation = "Rejected or requires collateral";
        }

        BigDecimal avgIncome = realIncome.compareTo(BigDecimal.ZERO) > 0
                ? realIncome.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return LoanRiskResult.builder()
                .score(score)
                .riskClass(riskClass)
                .recommendation(recommendation)
                .averageNetIncome(averageNetIncome)
                .declaredIncome(declaredIncome)
                .averageIncome(avgIncome)
                .totalExpenses(totalExpenses)
                .dti(dti)
                .build();
    }

    private String creditHistoryEvaluation(UUID userId) {
        CreditHistoryView view = loanRepository.getCreditHistoryByUserId(userId);
        if (view == null || view.getCreditStatus() == null) return "neutral";
        return view.getCreditStatus();
    }

    private static BigDecimal nvl(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x;
    }
}
