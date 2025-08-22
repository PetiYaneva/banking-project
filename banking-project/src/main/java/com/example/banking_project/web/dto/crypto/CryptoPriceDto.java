package com.example.banking_project.web.dto.crypto;

import java.math.BigDecimal;
import java.util.Map;
public record CryptoPriceDto(String id, Map<String, BigDecimal> prices) {}
