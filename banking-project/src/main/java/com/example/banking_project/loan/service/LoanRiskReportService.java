package com.example.banking_project.loan.service;

import com.example.banking_project.loan.repository.LoanRepository;
import com.example.banking_project.loan.view.CreditHistoryView;
import com.example.banking_project.transaction.service.ExpenseService;
import com.example.banking_project.transaction.service.IncomeService;
import com.example.banking_project.user.service.UserService;
import com.example.banking_project.web.dto.LoanRequest;
import com.example.banking_project.web.dto.LoanRiskReportData;
import com.example.banking_project.web.dto.LoanRiskResult;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanRiskReportService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final UserService userService;
    private final LoanRepository loanRepository;
    private final LoanRiskEngine loanRiskEngine;

    public byte[] generatePdf(LoanRiskReportData data) throws IOException, DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        document.add(new Paragraph("Loan Risk Report", titleFont));
        document.add(new Paragraph("Generated on: " + LocalDate.now()));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addRow(table, "User ID", data.getUserId());
        addRow(table, "Declared Income", data.getDeclaredIncome());
        addRow(table, "Real Income (6 months)", data.getCalculatedMonthlyIncome());
        addRow(table, "Total Expenses (6 months)", data.getCalculatedMonthlyExpenses());
        addRow(table, "Average Net Income", data.getAvailableIncome());
        addRow(table, "Obligations", data.getObligations());
        addRow(table, "DTI", data.getDti());
        addRow(table, "Credit History", data.getCreditHistory());
        addRow(table, "Score", data.getScore());
        addRow(table, "Risk Class", data.getRiskClass());
        addRow(table, "Recommendation", data.getRecommendation());

        document.add(table);
        document.close();
        return out.toByteArray();
    }

    private void addRow(PdfPTable table, String label, Object value) {
        table.addCell(label);
        table.addCell(value != null ? value.toString() : "N/A");
    }

    public byte[] generateExcel(LoanRiskReportData data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Loan Risk Report");

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("User ID", data.getUserId());
        values.put("Declared Income", data.getDeclaredIncome());
        values.put("Real Income (6 months)", data.getCalculatedMonthlyIncome());
        values.put("Total Expenses (6 months)", data.getCalculatedMonthlyExpenses());
        values.put("Average Net Income", data.getAvailableIncome());
        values.put("Obligations", data.getObligations());
        values.put("DTI", data.getDti());
        values.put("Credit History", data.getCreditHistory());
        values.put("Score", data.getScore());
        values.put("Risk Class", data.getRiskClass());
        values.put("Recommendation", data.getRecommendation());

        int rowIdx = 0;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue() != null ? entry.getValue().toString() : "N/A");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    public LoanRiskReportData prepareData(UUID userId) {
        BigDecimal declaredIncome = nvl(userService.findUserById(userId).getDeclaredIncome());
        BigDecimal monthlyIncome = nvl(incomeService.getIncomeForLastMonths(userId, 6));
        BigDecimal monthlyExpenses = nvl(expenseService.getExpensesForLastMonths(userId, 6));
        BigDecimal obligations = nvl(loanRepository.getMonthlyObligations(userId));

        BigDecimal dti = BigDecimal.ZERO;
        if (declaredIncome.compareTo(BigDecimal.ZERO) > 0) {
            dti = obligations.divide(declaredIncome, 2, RoundingMode.HALF_UP);
        }

        BigDecimal availableIncome = monthlyIncome.subtract(monthlyExpenses);
        String creditHistory = creditHistoryEvaluation(userId);

        // Оценката минава през LoanRiskEngine – НЕ през LoanService
        LoanRequest request = LoanRequest.builder()
                .userId(userId)
                .creditHistory(creditHistory)
                .monthlyIncome(declaredIncome)
                .monthlyObligations(obligations)
                .employmentType("unknown")
                .employmentYears(0)
                .collateral("none")
                .totalAmount(BigDecimal.ZERO)
                .build();

        LoanRiskResult result = loanRiskEngine.assess(request);

        return LoanRiskReportData.builder()
                .userId(userId)
                .declaredIncome(declaredIncome)
                .calculatedMonthlyIncome(monthlyIncome)
                .calculatedMonthlyExpenses(monthlyExpenses)
                .availableIncome(availableIncome)
                .obligations(obligations)
                .dti(dti)
                .creditHistory(creditHistory)
                .score(result.getScore())
                .riskClass(result.getRiskClass())
                .recommendation(result.getRecommendation())
                .generatedOn(LocalDate.now())
                .build();
    }

    private String creditHistoryEvaluation(UUID userId) {
        CreditHistoryView view = loanRepository.getCreditHistoryByUserId(userId);
        return (view == null || view.getCreditStatus() == null) ? "neutral" : view.getCreditStatus();
    }

    private static BigDecimal nvl(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x;
    }
}
