package com.example.banking_project.web.dto.crypto;

import com.example.banking_project.cryptocurrency.model.OrderSide;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceOrderRequest(
        @NotNull UUID userId,
        @NotBlank String iban,
        @NotBlank String symbol,
        @NotNull OrderSide side,
        @NotNull @DecimalMin("0.00000001") BigDecimal quantity
) {}


