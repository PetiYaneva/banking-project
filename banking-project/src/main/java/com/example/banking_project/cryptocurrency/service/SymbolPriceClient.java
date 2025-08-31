package com.example.banking_project.cryptocurrency.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface SymbolPriceClient {
    Optional<BigDecimal> getUsdPrice(String symbol);
    Map<String, BigDecimal> getUsdPrices(Collection<String> symbols);
}
