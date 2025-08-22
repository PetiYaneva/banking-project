package com.example.banking_project.loan.validation;

import com.example.banking_project.web.dto.LoanRequest;
import org.springframework.stereotype.Service;

@Service
public interface LoanValidationService {

    void validateLoanRequest(LoanRequest loanRequest);
}
