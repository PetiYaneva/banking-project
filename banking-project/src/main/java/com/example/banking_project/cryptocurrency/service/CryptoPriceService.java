package com.example.banking_project.cryptocurrency.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoPriceService {

    private static final long TTL_MS = 30_000L;

    private final Cache<String, PriceEntry> cryptoPriceCache;
    private final SymbolPriceImpl symbolPriceClient;

    public BigDecimal getLiveUsdPrice(String symbol) {
        String sym = symbol.toUpperCase(Locale.ROOT);

        PriceEntry hit = cryptoPriceCache.getIfPresent(sym);
        if (hit != null && hit.isFresh(TTL_MS)) {
            return hit.usd();
        }

        Optional<BigDecimal> fetched = symbolPriceClient.getUsdPrice(sym);
        BigDecimal price = fetched.orElseThrow(() ->
                new IllegalStateException("No live price for: " + sym));

        updatePrice(sym, price, "REST");
        return price;
    }

    public void updatePrice(String symbol, BigDecimal priceUsd, String source) {
        if (priceUsd == null || priceUsd.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        BigDecimal scaled = priceUsd.setScale(8, RoundingMode.HALF_UP);
        cryptoPriceCache.put(symbol.toUpperCase(Locale.ROOT),
                new PriceEntry(scaled, Instant.now(), source));
        log.debug("[PRICE] {} = {} USD (src={})", symbol.toUpperCase(Locale.ROOT), scaled, source);
    }

    public Optional<PriceEntry> peek(String symbol) {
        return Optional.ofNullable(cryptoPriceCache.getIfPresent(symbol.toUpperCase(Locale.ROOT)));
    }
}
