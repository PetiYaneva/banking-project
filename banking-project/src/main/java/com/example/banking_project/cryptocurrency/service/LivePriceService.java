package com.example.banking_project.cryptocurrency.service;

import com.example.banking_project.cryptocurrency.configuration.BinanceWebSocketClient;
import com.example.banking_project.cryptocurrency.model.CryptoSymbol;
import com.example.banking_project.cryptocurrency.repository.CryptoSymbolRepository;
import com.example.banking_project.web.dto.crypto.Ticker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LivePriceService {

    private final BinanceWebSocketClient ws;
    private final ObjectMapper mapper;
    private final CryptoSymbolRepository symbolRepo;
    private final CryptoPriceService priceService; // <-- да ъпдейтваме кеша

    private final Sinks.Many<Ticker> sink = Sinks.many().replay().latest();

    // runtime cache на символите, които следим
    private volatile List<CryptoSymbol> symbols = List.of();
    private volatile long symbolsHash = 0L;

    @PostConstruct
    public void init() {
        reloadSymbolsIfChanged();
        connect();
    }

    /** През 30 сек проверяваме: (1) жив ли е WS, (2) има ли промяна в символите. */
    @Scheduled(fixedDelay = 30_000)
    public void healthCheck() {
        boolean alive = ws.isAlive(30_000);
        boolean changed = reloadSymbolsIfChanged();
        if (!alive || changed) {
            log.warn("WS {}. Reconnecting...", !alive ? "stale" : "symbol set changed");
            connect();
        }
    }

    /** Публичен стрийм за фронтенд/други консумации */
    public Flux<Ticker> streamPrices() {
        return sink.asFlux();
    }


    private void connect() {
        if (symbols.isEmpty()) return;

        List<String> streams = symbols.stream()
                .map(s -> s.getBinancePair().toLowerCase() + "@trade")
                .toList();

        ws.connectForStreams(streams);

        ws.getPublisher().subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription sub;

            @Override public void onSubscribe(Flow.Subscription subscription) {
                this.sub = subscription; subscription.request(1);
            }

            @Override public void onNext(String json) {
                try {
                    JsonNode root = mapper.readTree(json);
                    JsonNode data = root.get("data");
                    if (data != null && data.isObject()) {
                        handleTrade(data);
                    } else {
                        handleTrade(root);
                    }
                } catch (Exception ex) {
                    log.warn("Failed to parse WS message: {}", ex.getMessage());
                    log.debug("Payload: {}", json, ex);
                } finally {
                    sub.request(1);
                }
            }

            private void handleTrade(JsonNode n) {
                if (!"trade".equals(opt(n,"e"))) return;
                String pair = opt(n,"s"); // BTCUSDT
                String priceStr = opt(n,"p");
                if (pair == null || priceStr == null) return;

                // намираме символа + coingecko id по pair
                symbols.stream()
                        .filter(cs -> cs.getBinancePair().equalsIgnoreCase(pair))
                        .findFirst()
                        .ifPresent(cs -> {
                            BigDecimal price = new BigDecimal(priceStr);
                            // a) емит към стрийма (id е coingeckoId за съвместимост с FE)
                            sink.tryEmitNext(new Ticker(cs.getCoingeckoId(), price));
                            // b) ъпдейт към кеша (по символ, напр. "BTC")
                            priceService.updatePrice(cs.getSymbol(), price, "WS");
                        });
            }

            @Override public void onError(Throwable t) {
                log.error("WS subscriber error: {}", t.getMessage(), t);
            }

            @Override public void onComplete() {
                log.warn("WS subscriber completed.");
            }
        });
    }

    /** Зарежда символите от БД и връща дали сетът се е променил. */
    private boolean reloadSymbolsIfChanged() {
        List<CryptoSymbol> fresh = symbolRepo.findAllByEnabledTrue();
        long hash = fresh.stream()
                .sorted(Comparator.comparing(CryptoSymbol::getSymbol))
                .map(cs -> cs.getSymbol()+"|"+cs.getBinancePair()+"|"+cs.getCoingeckoId())
                .collect(Collectors.joining(","))
                .hashCode();

        if (hash != symbolsHash) {
            this.symbols = fresh;
            this.symbolsHash = hash;
            log.info("Loaded {} symbols: {}", fresh.size(),
                    fresh.stream().map(CryptoSymbol::getSymbol).collect(Collectors.joining(",")));
            return true;
        }
        return false;
    }

    private static String opt(JsonNode n, String f) {
        JsonNode v = (n == null) ? null : n.get(f);
        return (v == null || v.isNull()) ? null : v.asText();
    }
}

