package com.example.banking_project.web.controllers;

import com.example.banking_project.loan.model.Loan;
import com.example.banking_project.loan.model.LoanStatus;
import com.example.banking_project.loan.service.LoanService;
import com.example.banking_project.loan.view.LoanView;
import com.example.banking_project.web.dto.LoanApplicationResponse;
import com.example.banking_project.web.dto.LoanRequest;
import com.example.banking_project.web.dto.LoanRiskResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    @GetMapping("/loans")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and hasAuthority('PROFILE_COMPLETED'))")
    public ResponseEntity<Optional<List<Loan>>> getLoansByUserId(@RequestParam UUID userId) {
        return ResponseEntity.ok(loanService.getLoansByUserId(userId));
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

    @GetMapping("/admin/credit-history/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getCreditHistoryStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(loanService.creditHistoryEvaluation(userId));
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getByStatus(@PathVariable LoanStatus status) {
        return ResponseEntity.ok(loanService.getLoansByStatus(status));
    }

    @GetMapping("/admin/status/{status}/next-payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getByStatusAndNextPaymentBetween(
            @PathVariable LoanStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(loanService.getLoansByStatusAndNextPaymentBetween(status, from, to));
    }

    @GetMapping("/admin/applied-between")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getAppliedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(loanService.getLoansAppliedBetween(from, to));
    }

    @GetMapping("/admin/final-between")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getFinalBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(loanService.getLoansFinalBetween(from, to));
    }

    @GetMapping("/admin/interest-between")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getInterestBetween(
            @RequestParam BigDecimal minRate,
            @RequestParam BigDecimal maxRate) {
        return ResponseEntity.ok(loanService.getLoansByInterestBetween(minRate, maxRate));
    }

    @GetMapping("/admin/total-between")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getTotalBetween(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {
        return ResponseEntity.ok(loanService.getLoansByTotalBetween(minAmount, maxAmount));
    }

    @GetMapping("/admin/remaining/gte")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getRemainingGte(@RequestParam BigDecimal minRemaining) {
        return ResponseEntity.ok(loanService.getLoansByRemainingGte(minRemaining));
    }

    @GetMapping("/admin/remaining/lte")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getRemainingLte(@RequestParam BigDecimal maxRemaining) {
        return ResponseEntity.ok(loanService.getLoansByRemainingLte(maxRemaining));
    }

    @GetMapping("/admin/missed-payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getByMissedPaymentsGt(@RequestParam int minMissed) {
        return ResponseEntity.ok(loanService.getLoansByMissedPaymentsGt(minMissed));
    }

    @GetMapping("/admin/due/views")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoanView>> getDueViews(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(loanService.getDueLoanViews(date));
    }

    @GetMapping("/admin/due")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getActiveDueBy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(loanService.getActiveLoansDueBy(date));
    }

    @GetMapping("/admin/repayment/account/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getByRepaymentAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(loanService.getLoansByRepaymentAccount(accountId));
    }

    @GetMapping("/admin/repayment/iban/{iban}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getByRepaymentIban(@PathVariable String iban) {
        return ResponseEntity.ok(loanService.getLoansByRepaymentIban(iban));
    }

    @GetMapping("/admin/remaining/total/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BigDecimal> getTotalRemainingByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(loanService.getTotalRemainingByUser(userId));
    }

    @GetMapping("/admin/overdue/count/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countOverdueByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(loanService.countOverdueByUser(userId));
    }

    @GetMapping("/admin/sorted/next-payment-asc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getAllOrderByNextPaymentAsc() {
        return ResponseEntity.ok(loanService.getAllOrderByNextPaymentAsc());
    }

    @GetMapping("/admin/sorted/remaining-desc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getAllOrderByRemainingDesc() {
        return ResponseEntity.ok(loanService.getAllOrderByRemainingDesc());
    }
}