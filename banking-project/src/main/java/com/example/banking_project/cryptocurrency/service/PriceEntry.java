package com.example.banking_project.cryptocurrency.service;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceEntry(BigDecimal usd, Instant ts, String source) {
    public boolean isFresh(long ttlMs) {
        return ts != null && (Instant.now().toEpochMilli() - ts.toEpochMilli()) < ttlMs;
    }
}

