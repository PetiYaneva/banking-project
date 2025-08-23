package com.example.banking_project.web.dto;

import com.example.banking_project.account.model.AccountType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateAccountRequest {
    private AccountType accountType;
    private String currencyCode;
    private BigDecimal initialBalance;
}
