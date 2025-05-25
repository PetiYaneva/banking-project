package com.example.banking_project.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {
    private String senderIban;
    private String receiverIban;
    private BigDecimal amount;
    private String description;
}
