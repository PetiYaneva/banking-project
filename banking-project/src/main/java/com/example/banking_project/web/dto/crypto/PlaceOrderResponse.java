package com.example.banking_project.web.dto.crypto;

import java.math.BigDecimal;

public record PlaceOrderResponse(
        String iban,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal priceUsd,
        BigDecimal priceBgn,
        BigDecimal totalCostBgn,
        String transactionId
) {}
