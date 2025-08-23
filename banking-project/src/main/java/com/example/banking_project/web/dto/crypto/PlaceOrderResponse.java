package com.example.banking_project.web.dto.crypto;

import com.example.banking_project.cryptocurrency.model.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceOrderResponse(
        UUID orderId,
        OrderStatus status,
        BigDecimal executedPrice,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal netAmount,
        String fiatCurrency
) {}
