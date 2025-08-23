package com.example.banking_project.web.dto.crypto;


import com.example.banking_project.cryptocurrency.model.OrderSide;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceOrderRequest(
        UUID userId,
        String iban,
        String asset,
        OrderSide side,
        BigDecimal quantity
) {}

