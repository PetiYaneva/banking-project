package com.example.banking_project.loan.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.service.AccountService;
import com.example.banking_project.loan.model.Loan;
import com.example.banking_project.loan.model.LoanStatus;
import com.example.banking_project.loan.repository.LoanRepository;
import com.example.banking_project.loan.validation.LoanValidationService;
import com.example.banking_project.loan.view.CreditHistoryView;
import com.example.banking_project.loan.view.LoanView;
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
import java.util.List;
import java.util.Optional;
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
        Account repayment = accountService.getAccountById(request.getRepaymentAccountId());
        loan.setRepaymentAccount(repayment);

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
        double r = monthlyRate.doubleValue();
        double p = principal.doubleValue();
        double pow = Math.pow(1.0 + r, -months);
        double m = p * r / (1.0 - pow);
        return BigDecimal.valueOf(m).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nvl(BigDecimal x) { return x == null ? BigDecimal.ZERO : x; }

    @Override public LoanRiskResult assessLoanRisk(LoanRequest request) { return loanRiskEngine.assess(request); }

    @Transactional
    @Override
    public LoanApplicationResponse applyForLoan(LoanRequest request) {
        LoanRiskResult eval = loanRiskEngine.assess(request);
        boolean approved = "Low Risk".equalsIgnoreCase(eval.getRiskClass());
        if (!approved) {
            return LoanApplicationResponse.builder()
                    .approved(false)
                    .riskClass(eval.getRiskClass())
                    .recommendation(eval.getRecommendation())
                    .build();
        }
        loanValidationService.validateLoanRequest(request);
        Loan loan = createApprovedLoanRecord(request);
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
        return view == null ? "neutral" : view.getCreditStatus();
    }

    @Override
    public byte[] generatePdfReport(UUID userId) {
        LoanRiskReportData data = riskReportService.prepareData(userId);
        try { return riskReportService.generatePdf(data); }
        catch (Exception e) { log.error("Failed to generate PDF loan report: {}", e.getMessage()); throw new RuntimeException("PDF generation failed", e); }
    }

    @Override
    public byte[] generateExcelReport(UUID userId) {
        LoanRiskReportData data = riskReportService.prepareData(userId);
        try { return riskReportService.generateExcel(data); }
        catch (Exception e) { log.error("Failed to generate Excel loan report: {}", e.getMessage()); throw new RuntimeException("Excel generation failed", e); }
    }

    @Override public BigDecimal getMonthlyObligation(UUID userId) { return loanRepository.getMonthlyObligations(userId); }
    @Override public BigDecimal getMonthlyObligationByLoanId(UUID loanId) { return loanRepository.getMonthlyObligationsByLoanId(loanId); }
    @Override public Optional<List<Loan>> getLoansByUserId(UUID userId) { return Optional.of(loanRepository.getLoansByUserId(userId)); }


    @Override public List<Loan> getLoansByStatus(LoanStatus status) {
        return loanRepository.findAllByLoanStatus(status);
    }

    @Override public List<Loan> getLoansByStatusAndNextPaymentBetween(LoanStatus status, LocalDate from, LocalDate to) {
        return loanRepository.findAllByLoanStatusAndNextDateOfPaymentBetween(status, from, to);
    }

    @Override public List<Loan> getLoansAppliedBetween(LocalDate from, LocalDate to) {
        return loanRepository.findAllByDateOfApplyingBetween(from, to);
    }

    @Override public List<Loan> getLoansFinalBetween(LocalDate from, LocalDate to) {
        return loanRepository.findAllByFinalDateBetween(from, to);
    }

    @Override public List<Loan> getLoansByInterestBetween(BigDecimal minRate, BigDecimal maxRate) {
        return loanRepository.findAllByInterestRateBetween(minRate, maxRate);
    }

    @Override public List<Loan> getLoansByTotalBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return loanRepository.findAllByTotalAmountBetween(minAmount, maxAmount);
    }

    @Override public List<Loan> getLoansByRemainingGte(BigDecimal minRemaining) {
        return loanRepository.findAllByRemainingAmountGreaterThanEqual(minRemaining);
    }

    @Override public List<Loan> getLoansByRemainingLte(BigDecimal maxRemaining) {
        return loanRepository.findAllByRemainingAmountLessThanEqual(maxRemaining);
    }

    @Override public List<Loan> getLoansByMissedPaymentsGt(int minMissed) {
        return loanRepository.findAllByMissedPaymentsGreaterThan(minMissed);
    }

    @Override public List<LoanView> getDueLoanViews(LocalDate date) {
        return loanRepository.findDueLoanViews(date);
    }

    @Override public List<Loan> getActiveLoansDueBy(LocalDate date) {
        return loanRepository.findAllByLoanStatusAndNextDateOfPaymentLessThanEqual(LoanStatus.ACTIVE, date);
    }

    @Override public List<Loan> getLoansByRepaymentAccount(UUID accountId) {
        return loanRepository.findAllByRepaymentAccount_Id(accountId);
    }

    @Override public List<Loan> getLoansByRepaymentIban(String iban) {
        return loanRepository.findAllByRepaymentIban(iban);
    }

    @Override public BigDecimal getTotalRemainingByUser(UUID userId) {
        return loanRepository.getTotalRemainingByUser(userId);
    }

    @Override public long countOverdueByUser(UUID userId) {
        return loanRepository.countOverdueByUser(userId);
    }

    @Override public List<Loan> getAllOrderByNextPaymentAsc() {
        return loanRepository.findAllByOrderByNextDateOfPaymentAsc();
    }

    @Override public List<Loan> getAllOrderByRemainingDesc() {
        return loanRepository.findAllByOrderByRemainingAmountDesc();
    }
}
