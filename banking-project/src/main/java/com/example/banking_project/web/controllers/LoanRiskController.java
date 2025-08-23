package com.example.banking_project.web.controllers;

import com.example.banking_project.loan.service.LoanService;
import com.example.banking_project.web.dto.LoanApplicationResponse;
import com.example.banking_project.web.dto.LoanRequest;
import com.example.banking_project.web.dto.LoanRiskResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanRiskController {

    private final LoanService loanService;

    @PostMapping(value = "/risk-assessment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    public ResponseEntity<LoanRiskResult> assessRisk(@Valid @RequestBody LoanRequest request) {
        LoanRiskResult result = loanService.assessLoanRisk(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/apply",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    public ResponseEntity<LoanApplicationResponse> apply(@Valid @RequestBody LoanRequest request) {
        LoanApplicationResponse resp = loanService.applyForLoan(request);
        if (resp.isApproved() && resp.getLoanId() != null) {
            return ResponseEntity
                    .created(URI.create("/api/loans/" + resp.getLoanId()))
                    .body(resp);
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/risk-report/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam UUID userId) {
        byte[] pdf = loanService.generatePdfReport(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loan_risk_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

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
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    public ResponseEntity<BigDecimal> getUserMonthlyObligations(@PathVariable UUID userId) {
        BigDecimal obligations = loanService.getMonthlyObligation(userId);
        return ResponseEntity.ok(obligations != null ? obligations : BigDecimal.ZERO);
    }

    @GetMapping("/obligations/loan/{loanId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    public ResponseEntity<BigDecimal> getLoanMonthlyObligations(@PathVariable UUID loanId) {
        BigDecimal obligations = loanService.getMonthlyObligationByLoanId(loanId);
        return ResponseEntity.ok(obligations != null ? obligations : BigDecimal.ZERO);
    }
}