package com.example.banking_project.loan.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface LoanView {
    UUID getId();
    UUID getUserId();
    BigDecimal getMonthlyPayment();
    BigDecimal getRemainingAmount();
    LocalDate getNextDateOfPayment();
    String getLoanStatus();
    BigDecimal getRepaymentBalance();
    UUID getRepaymentAccountId();
    String getRepaymentIban();
}
