package com.example.banking_project.cryptocurrency.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

@Component
@Slf4j
public class SymbolPriceImpl implements SymbolPriceClient{

    private final WebClient webClient = WebClient.builder().build();

    @Value("${crypto.coingecko.base-url:https://api.coingecko.com/api/v3}")
    private String coingeckoBase;

    private static final Map<String, String> CG_ID = Map.ofEntries(
            Map.entry("BTC", "bitcoin"),
            Map.entry("ETH", "ethereum"),
            Map.entry("BNB", "binancecoin"),
            Map.entry("SOL", "solana"),
            Map.entry("ADA", "cardano"),
            Map.entry("XRP", "ripple")
    );

    @Override
    public Optional<BigDecimal> getUsdPrice(String symbol) {
        String id = CG_ID.get(symbol.toUpperCase(Locale.ROOT));
        if (id == null) return Optional.empty();

        try {
            Map<?, ?> res = webClient.get()
                    .uri(coingeckoBase + "/simple/price?ids={ids}&vs_currencies=usd", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(err -> {
                        log.warn("CG fetch error: {}", err.toString());
                        return Mono.empty();
                    })
                    .blockOptional().orElse(Map.of());

            Map<?, ?> obj = (Map<?, ?>) res.get(id);
            if (obj == null) return Optional.empty();

            Object usd = obj.get("usd");
            if (usd == null) return Optional.empty();

            return Optional.of(new BigDecimal(usd.toString()));
        } catch (Exception ex) {
            log.warn("CG fetch exception: {}", ex.toString());
            return Optional.empty();
        }
    }

    @Override
    public Map<String, BigDecimal> getUsdPrices(Collection<String> symbols) {
        List<String> ids = symbols.stream()
                .map(s -> CG_ID.get(s.toUpperCase(Locale.ROOT)))
                .filter(Objects::nonNull)
                .distinct().toList();
        if (ids.isEmpty()) return Map.of();

        Map<String, String> rev = new HashMap<>();
        CG_ID.forEach((sym, id) -> rev.put(id, sym));

        Map<String, BigDecimal> out = new HashMap<>();
        try {
            Map<String, Map<String, Object>> res = webClient.get()
                    .uri(coingeckoBase + "/simple/price?ids={ids}&vs_currencies=usd",
                            String.join(",", ids))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .cast((Class<Map<String, Map<String, Object>>>) (Class<?>) Map.class)
                    .onErrorResume(err -> {
                        log.warn("CG batch fetch error: {}", err.toString());
                        return Mono.empty();
                    })
                    .blockOptional().orElse(Map.of());

            res.forEach((id, obj) -> {
                Object usd = obj.get("usd");
                if (usd != null) {
                    String sym = rev.get(id);
                    out.put(sym, new BigDecimal(usd.toString()));
                }
            });
        } catch (Exception ex) {
            log.warn("CG batch fetch exception: {}", ex.toString());
        }
        return out;
    }
}
