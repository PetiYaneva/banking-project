package com.example.banking_project.cryptocurrency.configuration;

import com.example.banking_project.web.dto.crypto.CryptoHistoryDto;
import com.example.banking_project.web.dto.crypto.CryptoHistoryPointDto;
import com.example.banking_project.web.dto.crypto.CryptoPriceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

@ConditionalOnProperty(name = "crypto.source", havingValue = "coingecko")
@Component
public class CoinGeckoClient implements MarketDataClient {

    private final WebClient client;

    public CoinGeckoClient(WebClient defaultWebClient,
                           @Value("${crypto.coingecko.base-url}") String baseUrl) {
        this.client = defaultWebClient.mutate().baseUrl(baseUrl).build();
    }

    @Override
    public Mono<List<CryptoPriceDto>> simplePrice(String idsCsv, String vsCurrenciesCsv) {
        return client.get()
                .uri(uri -> uri.path("/simple/price")
                        .queryParam("ids", idsCsv)
                        .queryParam("vs_currencies", vsCurrenciesCsv)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(raw -> {
                    Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) raw;
                    List<CryptoPriceDto> out = new ArrayList<>();
                    for (var e : map.entrySet()) {
                        Map<String, BigDecimal> prices = new LinkedHashMap<>();
                        e.getValue().forEach((k, v) -> prices.put(k.toLowerCase(Locale.ROOT), toBigDecimal(v)));
                        out.add(new CryptoPriceDto(e.getKey(), prices));
                    }
                    out.sort(Comparator.comparing(CryptoPriceDto::id));
                    return out;
                });
    }

    @Override
    public Mono<CryptoHistoryDto> history(String id, String vsCurrency, String days) {
        return client.get()
                .uri(uri -> uri.path("/coins/{id}/market_chart")
                        .queryParam("vs_currency", vsCurrency.toLowerCase(Locale.ROOT))
                        .queryParam("days", days)
                        .build(id))
                .retrieve()
                .bodyToMono(Map.class)
                .map(raw -> {
                    List<List<Object>> prices = (List<List<Object>>) raw.getOrDefault("prices", List.of());
                    List<CryptoHistoryPointDto> points = new ArrayList<>(prices.size());
                    for (List<Object> p : prices) {
                        long ts = ((Number) p.get(0)).longValue();
                        BigDecimal price = toBigDecimal(p.get(1));
                        points.add(new CryptoHistoryPointDto(ts, price));
                    }
                    return new CryptoHistoryDto(id, vsCurrency.toLowerCase(Locale.ROOT), points);
                });
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(v.toString());
    }
}
