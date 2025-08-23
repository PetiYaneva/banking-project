package com.example.banking_project.cryptocurrency.configuration;

import com.example.banking_project.web.dto.crypto.CryptoHistoryDto;
import com.example.banking_project.web.dto.crypto.CryptoPriceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

@ConditionalOnProperty(name = "crypto.source", havingValue = "binance")
@Component
public class BinanceClient implements MarketDataClient {

    private final WebClient client;

    // Речник: CoinGecko-like ID → Binance Symbol (USDT pair)
    private static final Map<String, String> SYMBOL_MAP = Map.ofEntries(
            Map.entry("bitcoin", "BTCUSDT"),
            Map.entry("ethereum", "ETHUSDT"),
            Map.entry("binancecoin", "BNBUSDT"),
            Map.entry("ripple", "XRPUSDT"),
            Map.entry("cardano", "ADAUSDT"),
            Map.entry("solana", "SOLUSDT"),
            Map.entry("dogecoin", "DOGEUSDT"),
            Map.entry("polkadot", "DOTUSDT"),
            Map.entry("polygon", "MATICUSDT"),
            Map.entry("litecoin", "LTCUSDT"),
            Map.entry("tron", "TRXUSDT"),
            Map.entry("avalanche-2", "AVAXUSDT"),
            Map.entry("chainlink", "LINKUSDT"),
            Map.entry("uniswap", "UNIUSDT"),
            Map.entry("stellar", "XLMUSDT"),
            Map.entry("monero", "XMRUSDT"),
            Map.entry("okb", "OKBUSDT"),
            Map.entry("internet-computer", "ICPUSDT"),
            Map.entry("aptos", "APTUSDT"),
            Map.entry("near", "NEARUSDT")
    );

    public BinanceClient(WebClient defaultWebClient,
                         @Value("${crypto.binance.rest-base-url}") String baseUrl) {
        this.client = defaultWebClient.mutate().baseUrl(baseUrl).build();
    }

    @Override
    public Mono<List<CryptoPriceDto>> simplePrice(String idsCsv, String vsCurrenciesCsv) {
        List<String> ids = Arrays.stream(idsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        return Flux.fromIterable(ids)
                .flatMap(id -> {
                    String symbol = SYMBOL_MAP.getOrDefault(id.toLowerCase(), "BTCUSDT"); // fallback към BTC
                    return client.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/api/v3/ticker/price")
                                    .queryParam("symbol", symbol)
                                    .build())
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(m -> {
                                String priceStr = String.valueOf(m.get("price"));
                                BigDecimal usd = new BigDecimal(priceStr); // USDT ~= USD
                                return new CryptoPriceDto(id, Map.of("usd", usd));
                            });
                })
                .collectList();
    }

    @Override
    public Mono<CryptoHistoryDto> history(String id, String vsCurrency, String days) {
        return Mono.error(new UnsupportedOperationException(
                "Binance history not implemented; consider using CoinGecko or Binance /klines"));
    }
}
