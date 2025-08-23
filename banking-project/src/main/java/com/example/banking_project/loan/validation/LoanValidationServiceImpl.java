package com.example.banking_project.loan.validation;

import com.example.banking_project.web.dto.LoanRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class LoanValidationServiceImpl implements LoanValidationService{
    @Override
    public void validateLoanRequest(LoanRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("totalAmount must be > 0");
        }
        if (request.getTermMonths() <= 0) {
            throw new IllegalArgumentException("termMonths must be > 0");
        }
        if (request.getInterestRate() == null || request.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("interestRate must be >= 0");
        }
    }
}
