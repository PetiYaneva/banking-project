package com.example.banking_project.cryptocurrency.service;

import com.example.banking_project.cryptocurrency.configuration.BinanceWebSocketClient;
import com.example.banking_project.web.dto.crypto.Ticker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

@Service
public class LivePriceService {
    private static final Logger log = LoggerFactory.getLogger(LivePriceService.class);

    private final BinanceWebSocketClient ws;
    private final ObjectMapper mapper;
    private final Sinks.Many<Ticker> sink = Sinks.many().replay().latest();

    // символ (Binance) -> твой id
    private static final Map<String, String> SYMBOL_TO_ID = Map.ofEntries(
            Map.entry("BTCUSDT", "bitcoin"),
            Map.entry("ETHUSDT", "ethereum"),
            Map.entry("BNBUSDT", "binancecoin"),
            Map.entry("XRPUSDT", "ripple"),
            Map.entry("ADAUSDT", "cardano"),
            Map.entry("SOLUSDT", "solana"),
            Map.entry("DOGEUSDT", "dogecoin"),
            Map.entry("DOTUSDT", "polkadot"),
            Map.entry("MATICUSDT", "polygon"),
            Map.entry("LTCUSDT", "litecoin"),
            Map.entry("TRXUSDT", "tron"),
            Map.entry("AVAXUSDT", "avalanche-2"),
            Map.entry("LINKUSDT", "chainlink"),
            Map.entry("UNIUSDT", "uniswap"),
            Map.entry("XLMUSDT", "stellar"),
            Map.entry("XMRUSDT", "monero"),
            Map.entry("OKBUSDT", "okb"),
            Map.entry("ICPUSDT", "internet-computer"),
            Map.entry("APTUSDT", "aptos"),
            Map.entry("NEARUSDT", "near")
    );

    public LivePriceService(BinanceWebSocketClient ws, ObjectMapper mapper) {
        this.ws = ws;
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        // 1) комбиниран stream за 20 символа
        List<String> streams = SYMBOL_TO_ID.keySet().stream()
                .map(s -> s.toLowerCase() + "@trade") // BTCUSDT -> btcusdt@trade
                .toList();
        ws.connectForStreams(streams);

        // 2) абонамент
        ws.getPublisher().subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription sub;

            @Override public void onSubscribe(Flow.Subscription subscription) {
                this.sub = subscription; subscription.request(1);
            }

            @Override public void onNext(String json) {
                try {
                    JsonNode root = mapper.readTree(json);

                    // Комбинираният WS връща {"stream":"btcusdt@trade","data":{...}}
                    JsonNode data = root.get("data");
                    if (data != null && data.isObject()) {
                        handleTrade(data);
                    } else {
                        // fallback: ако по някаква причина е директното trade събитие
                        handleTrade(root);
                    }
                } catch (Exception ex) {
                    log.warn("Failed to parse WS message (len={}): {}", json.length(), ex.getMessage());
                    log.debug("Payload: {}", json, ex);
                } finally {
                    sub.request(1);
                }
            }

            private void handleTrade(JsonNode n) {
                if (!"trade".equals(opt(n, "e"))) return;
                String symbol = opt(n, "s");         // BTCUSDT
                String id = SYMBOL_TO_ID.get(symbol);
                String priceStr = opt(n, "p");       // цена като текст
                if (id != null && priceStr != null) {
                    sink.tryEmitNext(new Ticker(id, new BigDecimal(priceStr)));
                }
            }

            @Override public void onError(Throwable t) {
                log.error("WS subscriber error: {}", t.getMessage(), t);
                // оставяме reconnect логиката да се тригърне от healthCheck()
            }

            @Override public void onComplete() {
                log.warn("WS subscriber completed.");
            }
        });
    }

    /** Health check на всеки 30 сек – ако няма събития, извикай отново connectForStreams(...) */
    @Scheduled(fixedDelay = 30_000)
    public void healthCheck() {
        if (!ws.isAlive(30_000)) {
            log.warn("WS considered stale. Reconnecting...");
            List<String> streams = SYMBOL_TO_ID.keySet().stream()
                    .map(s -> s.toLowerCase() + "@trade")
                    .toList();
            ws.connectForStreams(streams);
        }
    }

    public Flux<Ticker> streamPrices() {
        return sink.asFlux();
    }

    private static String opt(JsonNode n, String f) {
        JsonNode v = (n == null) ? null : n.get(f);
        return (v == null || v.isNull()) ? null : v.asText();
    }
}
