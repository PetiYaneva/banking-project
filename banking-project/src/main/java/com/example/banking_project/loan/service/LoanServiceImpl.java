package com.example.banking_project.loan.service;

import com.example.banking_project.account.service.AccountService;
import com.example.banking_project.loan.model.Loan;
import com.example.banking_project.loan.model.LoanStatus;
import com.example.banking_project.loan.repository.LoanRepository;
import com.example.banking_project.loan.validation.LoanValidationService;
import com.example.banking_project.loan.view.CreditHistoryView;
import com.example.banking_project.user.model.User;
import com.example.banking_project.user.service.UserService;
import com.example.banking_project.web.dto.LoanApplicationResponse;
import com.example.banking_project.web.dto.LoanRequest;
import com.example.banking_project.web.dto.LoanRiskReportData;
import com.example.banking_project.web.dto.LoanRiskResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanRiskReportService riskReportService;
    private final LoanRiskEngine loanRiskEngine;
    private final AccountService accountService;
    private final UserService userService;
    private final LoanValidationService loanValidationService;

    private Loan createApprovedLoanRecord(LoanRequest request) {
        UUID userId = request.getUserId();
        User user = userService.findUserById(userId);

        BigDecimal totalAmount = nvl(request.getTotalAmount());
        int months = request.getTermMonths();
        BigDecimal annualRate = nvl(request.getInterestRate());

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = computeAnnuity(totalAmount, monthlyRate, months);

        LocalDate applyDate = LocalDate.now();
        LocalDate finalDate = request.getFinalDate() != null ? request.getFinalDate() : applyDate.plusMonths(months);
        LocalDate nextPayment = applyDate.plusMonths(1);

        Loan loan = new Loan();
        loan.setDateOfApplying(applyDate);
        loan.setFinalDate(finalDate);
        loan.setNextDateOfPayment(nextPayment);
        loan.setTotalAmount(totalAmount);
        loan.setRemainingAmount(totalAmount);
        loan.setInterestRate(annualRate);
        loan.setMonthlyPayment(monthlyPayment);
        loan.setTermMonths(months);
        loan.setMissedPayments(0);
        loan.setLoanStatus(LoanStatus.ACTIVE);
        loan.setUser(user);

        Loan saved = loanRepository.save(loan);
        log.info("Created approved loan for user {} with monthlyPayment={} and term={} months",
                userId, monthlyPayment, months);

        accountService.createCreditAccount(request, userId);

        return saved;
    }


    private static BigDecimal computeAnnuity(BigDecimal principal, BigDecimal monthlyRate, int months) {
        if (months <= 0) throw new IllegalArgumentException("Months must be > 0");
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        // M = P * r / (1 - (1 + r)^-n)
        double r = monthlyRate.doubleValue();
        double p = principal.doubleValue();
        double pow = Math.pow(1.0 + r, -months);
        double m = p * r / (1.0 - pow);
        return BigDecimal.valueOf(m).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nvl(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x;
    }

    @Override
    public LoanRiskResult assessLoanRisk(LoanRequest request) {
        return loanRiskEngine.assess(request);
    }

    @Transactional
    @Override
    public LoanApplicationResponse applyForLoan(LoanRequest request) {
        LoanRiskResult eval = loanRiskEngine.assess(request);

        // Одобряваме само при Low Risk (смени логиката, ако искаш и Medium)
        boolean approved = "Low Risk".equalsIgnoreCase(eval.getRiskClass());
        if (!approved) {
            return LoanApplicationResponse.builder()
                    .approved(false)
                    .riskClass(eval.getRiskClass())
                    .recommendation(eval.getRecommendation())
                    .build();
        }

        loanValidationService.validateLoanRequest(request);
        Loan loan = createApprovedLoanRecord(request); // връща Loan

        return LoanApplicationResponse.builder()
                .approved(true)
                .riskClass(eval.getRiskClass())
                .recommendation(eval.getRecommendation())
                .loanId(loan.getId())
                .monthlyPayment(loan.getMonthlyPayment())
                .build();
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
