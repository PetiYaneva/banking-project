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
import java.util.stream.Collectors;

@ConditionalOnProperty(name = "crypto.source", havingValue = "binance")
@Component
public class BinanceClient implements MarketDataClient {

    private final WebClient client;

    private static final Map<String, String> ID_TO_PAIR = Map.ofEntries(
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
                         @Value("${crypto.binance.rest-base-url:https://api.binance.com}") String baseUrl) {
        this.client = defaultWebClient.mutate().baseUrl(baseUrl).build();
    }

    private static String toUsdtPair(String symbol) {
        String s = symbol.toUpperCase(Locale.ROOT);
        String fromId = ID_TO_PAIR.get(symbol.toLowerCase(Locale.ROOT));
        return (fromId != null) ? fromId : (s.endsWith("USDT") ? s : s + "USDT");
    }

    public Mono<List<CryptoPriceDto>> simplePrice(String idsCsv, String vsCurrenciesCsv) {
        List<String> ids = Arrays.stream(idsCsv.split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList();

        return Flux.fromIterable(ids)
                .flatMap(id -> client.get()
                        .uri(uriBuilder -> uriBuilder.path("/api/v3/ticker/price")
                                .queryParam("symbol", ID_TO_PAIR.getOrDefault(id.toLowerCase(), "BTCUSDT"))
                                .build())
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(m -> {
                            String priceStr = String.valueOf(m.get("price"));
                            BigDecimal usd = new BigDecimal(priceStr); // USDT ~= USD
                            return new CryptoPriceDto(id, Map.of("usd", usd));
                        }))
                .collectList();
    }

    public Mono<CryptoHistoryDto> history(String id, String vsCurrency, String days) {
        return Mono.error(new UnsupportedOperationException(
                "Binance history not implemented; consider /api/v3/klines"));
    }
}
