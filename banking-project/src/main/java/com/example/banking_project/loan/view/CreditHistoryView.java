package com.example.banking_project.loan.view;

import java.time.LocalDate;
import java.util.UUID;

public interface CreditHistoryView {

    UUID getUserId();
    int getTotalLoans();
    int getOverdueLoans();
    String getCreditStatus();
    LocalDate getLastLoanDate();
}
