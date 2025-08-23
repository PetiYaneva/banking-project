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
) {
    // helper за да присадим userId от JWT в контролера
    public PlaceOrderRequest withUserId(UUID newUserId) {
        return new PlaceOrderRequest(newUserId, this.iban, this.asset, this.side, this.quantity);
    }
}

