package com.example.banking_project.web.dto.crypto;

import java.math.BigDecimal;
public record CryptoHistoryPointDto(long timestamp, BigDecimal price) {}
