package com.example.banking_project.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TransferResponse {
    private String senderIban;
    private String receiverIban;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String status;
    private LocalDate createdOn;
    private String transactionId;
}