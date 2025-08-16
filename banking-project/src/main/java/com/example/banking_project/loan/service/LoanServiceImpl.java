package com.example.banking_project.loan.service;

import com.example.banking_project.loan.repository.LoanRepository;
import com.example.banking_project.loan.view.CreditHistoryView;
import com.example.banking_project.transaction.service.ExpenseService;
import com.example.banking_project.transaction.service.IncomeService;
import com.example.banking_project.web.dto.LoanRequest;
import com.example.banking_project.web.dto.LoanRiskReportData;
import com.example.banking_project.web.dto.LoanRiskResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final LoanRiskReportService riskReportService;

    @Override
    public LoanRiskResult assessLoanRisk(LoanRequest request) {
        int score = 0;

        String creditHistory = creditHistoryEvaluation(request.getUserId());
        if ("good".equalsIgnoreCase(creditHistory)) {
            score += 20;
        } else if ("neutral".equalsIgnoreCase(creditHistory)) {
            score += 10;
        }

        BigDecimal declaredIncome = request.getMonthlyIncome();
        if (declaredIncome.compareTo(new BigDecimal("5000")) > 0) {
            score += 20;
        } else if (declaredIncome.compareTo(new BigDecimal("3000")) >= 0) {
            score += 15;
        } else if (declaredIncome.compareTo(new BigDecimal("1500")) >= 0) {
            score += 10;
        } else {
            score += 5;
        }

        // Реални стойности за последните 6 месеца
        BigDecimal realIncome = incomeService.getIncomeForLastMonths(request.getUserId(), 6);
        BigDecimal totalExpenses = expenseService.getExpensesForLastMonths(request.getUserId(), 6);

        if (realIncome.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("User {} has no recorded income in the last 6 months.", request.getUserId());
            score -= 20;
        }

        BigDecimal averageNetIncome = realIncome.subtract(totalExpenses)
                .divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);

        // DTI – реални разходи спрямо деклариран доход
        BigDecimal dti = request.getMonthlyObligations()
                .divide(declaredIncome, 2, RoundingMode.HALF_UP);

        if (dti.compareTo(new BigDecimal("0.3")) < 0) {
            score += 20;
        } else if (dti.compareTo(new BigDecimal("0.5")) <= 0) {
            score += 10;
        }

        // Employment duration
        if (request.getEmploymentYears() > 3) {
            score += 15;
        } else if (request.getEmploymentYears() >= 1) {
            score += 10;
        } else {
            score += 5;
        }

        // Employment type
        if ("permanent".equalsIgnoreCase(request.getEmploymentType())) {
            score += 10;
        } else if ("temporary".equalsIgnoreCase(request.getEmploymentType())) {
            score += 5;
        }

        // Collateral
        if ("property".equalsIgnoreCase(request.getCollateral())) {
            score += 10;
        } else if ("guarantor".equalsIgnoreCase(request.getCollateral())) {
            score += 5;
        }

        // Проверка за реални приходи
        if (averageNetIncome.compareTo(BigDecimal.ZERO) <= 0) {
            score -= 15;
            log.warn("User {} has zero or negative net income.", request.getUserId());
        } else if (averageNetIncome.compareTo(declaredIncome.multiply(BigDecimal.valueOf(0.5))) < 0) {
            score -= 10;
            log.warn("User {} has low real net income compared to declared.", request.getUserId());
        }

        String riskClass, recommendation;
        if (score >= 75) {
            riskClass = "Low Risk";
            recommendation = "Approved under standard conditions";
        } else if (score >= 50) {
            riskClass = "Medium Risk";
            recommendation = "Approved with additional conditions";
        } else {
            riskClass = "High Risk";
            recommendation = "Rejected or requires collateral";
        }

        return new LoanRiskResult(
                score,
                riskClass,
                recommendation,
                averageNetIncome,
                declaredIncome,
                realIncome.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP), // avg income
                totalExpenses,
                dti
        );
    }

    @Override
    public String creditHistoryEvaluation(UUID userId) {
        CreditHistoryView view = loanRepository.getCreditHistoryByUserId(userId);

        if (view == null) {
            return "neutral";
        }

        return view.getCreditStatus();
    }

    @Override
    public byte[] generatePdfReport(UUID userId) {
        LoanRiskReportData data = riskReportService.prepareData(userId);
        try {
            return riskReportService.generatePdf(data);
        } catch (Exception e) {
            log.error("Failed to generate PDF loan report: {}", e.getMessage());
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    @Override
    public byte[] generateExcelReport(UUID userId) {
        LoanRiskReportData data = riskReportService.prepareData(userId);
        try {
            return riskReportService.generateExcel(data);
        } catch (Exception e) {
            log.error("Failed to generate Excel loan report: {}", e.getMessage());
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    @Override
    public BigDecimal getMonthlyObligation(UUID userId) {
        return loanRepository.getMonthlyObligations(userId);
    }

    @Override
    public BigDecimal getMonthlyObligationByLoanId(UUID loanId) {
        return loanRepository.getMonthlyObligationsByLoanId(loanId);
    }
}
