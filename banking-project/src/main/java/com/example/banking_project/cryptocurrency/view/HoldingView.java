package com.example.banking_project.cryptocurrency.view;

import java.math.BigDecimal;

public record HoldingView(
        String symbol,
        BigDecimal quantity,
        BigDecimal priceUsd,      // текуща цена
        BigDecimal priceBgn,
        BigDecimal valueUsd,      // quantity * priceUsd
        BigDecimal valueBgn       // quantity * priceBgn
) {}

