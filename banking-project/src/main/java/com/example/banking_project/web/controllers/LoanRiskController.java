package com.example.banking_project.web.controllers;

import com.example.banking_project.loan.service.LoanService;
import com.example.banking_project.web.dto.LoanRequest;
import com.example.banking_project.web.dto.LoanRiskResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
public class LoanRiskController {

    private final LoanService loanService;

    public LoanRiskController(LoanService loanService) {
        this.loanService = loanService;
    }

    // 👥 USER & ADMIN
    @PostMapping("/risk-assessment")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LoanRiskResult> assessRisk(@RequestBody LoanRequest request) {
        LoanRiskResult result = loanService.assessLoanRisk(request);
        return ResponseEntity.ok(result);
    }

    // 👑 ADMIN only
    @GetMapping("/risk-report/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam UUID userId) {
        byte[] pdf = loanService.generatePdfReport(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loan_risk_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // 👑 ADMIN only
    @GetMapping("/risk-report/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadExcel(@RequestParam UUID userId) {
        byte[] excel = loanService.generateExcelReport(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loan_risk_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/obligations/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BigDecimal> getUserMonthlyObligations(@PathVariable UUID userId) {
        BigDecimal obligations = loanService.getMonthlyObligation(userId);
        return ResponseEntity.ok(obligations != null ? obligations : BigDecimal.ZERO);
    }

    @GetMapping("/obligations/loan/{loanId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BigDecimal> getLoanMonthlyObligations(@PathVariable UUID loanId) {
        BigDecimal obligations = loanService.getMonthlyObligationByLoanId(loanId);
        return ResponseEntity.ok(obligations != null ? obligations : BigDecimal.ZERO);
    }
}
