package com.example.banking_project.loan.service;

import com.example.banking_project.loan.model.Loan;
import com.example.banking_project.loan.model.LoanStatus;
import com.example.banking_project.loan.view.LoanView;
import com.example.banking_project.web.dto.LoanApplicationResponse;
import com.example.banking_project.web.dto.LoanRequest;
import com.example.banking_project.web.dto.LoanRiskResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface LoanService {

    LoanRiskResult assessLoanRisk(LoanRequest request);
    LoanApplicationResponse applyForLoan(LoanRequest request);
    String creditHistoryEvaluation(UUID userId);

    byte[] generatePdfReport(UUID userId);
    byte[] generateExcelReport(UUID userId);

    BigDecimal getMonthlyObligation(UUID userId);
    BigDecimal getMonthlyObligationByLoanId(UUID loanId);

    Optional<List<Loan>> getLoansByUserId(UUID userId);

    List<Loan> getLoansByStatus(LoanStatus status);
    List<Loan> getLoansByStatusAndNextPaymentBetween(LoanStatus status, LocalDate from, LocalDate to);
    List<Loan> getLoansAppliedBetween(LocalDate from, LocalDate to);
    List<Loan> getLoansFinalBetween(LocalDate from, LocalDate to);
    List<Loan> getLoansByInterestBetween(BigDecimal minRate, BigDecimal maxRate);
    List<Loan> getLoansByTotalBetween(BigDecimal minAmount, BigDecimal maxAmount);
    List<Loan> getLoansByRemainingGte(BigDecimal minRemaining);
    List<Loan> getLoansByRemainingLte(BigDecimal maxRemaining);
    List<Loan> getLoansByMissedPaymentsGt(int minMissed);

    List<LoanView> getDueLoanViews(LocalDate date);

    List<Loan> getActiveLoansDueBy(LocalDate date);

    List<Loan> getLoansByRepaymentAccount(UUID accountId);
    List<Loan> getLoansByRepaymentIban(String iban);

    BigDecimal getTotalRemainingByUser(UUID userId);
    long countOverdueByUser(UUID userId);

    List<Loan> getAllOrderByNextPaymentAsc();
    List<Loan> getAllOrderByRemainingDesc();
}
