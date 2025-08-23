package com.example.banking_project.web.dto.crypto;

import java.math.BigDecimal;
import java.util.UUID;

public record HoldingView(
        UUID accountId,
        String iban,
        String asset,
        BigDecimal quantity,
        BigDecimal avgPrice,
        BigDecimal marketPrice,
        BigDecimal marketValue,
        BigDecimal unrealizedPnl
) {}