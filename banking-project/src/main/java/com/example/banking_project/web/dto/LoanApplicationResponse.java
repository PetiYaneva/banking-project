package com.example.banking_project.web.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoanApplicationResponse {
    private boolean approved;
    private String riskClass;
    private String recommendation;
    private UUID loanId;
    private BigDecimal monthlyPayment;
}
