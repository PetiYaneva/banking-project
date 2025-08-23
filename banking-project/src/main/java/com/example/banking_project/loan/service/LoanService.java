package com.example.banking_project.loan.service;

import com.example.banking_project.web.dto.LoanApplicationResponse;
import com.example.banking_project.web.dto.LoanRequest;
import com.example.banking_project.web.dto.LoanRiskReportData;
import com.example.banking_project.web.dto.LoanRiskResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
}
