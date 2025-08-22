package com.example.banking_project.cryptocurrency.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class BinanceWebSocketClient {
    private static final Logger log = LoggerFactory.getLogger(BinanceWebSocketClient.class);

    private final SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
    private final AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();
    private volatile long lastMessageAt = 0L;

    /** Стар метод – държа го за обратна съвместимост (единичен поток). */
    public synchronized void connect() {
        connectForStreams(java.util.List.of("btcusdt@trade"));
    }

    /** Нов метод: приема колекция от stream-и (напр. btcusdt@trade, ethusdt@trade, …) */
    public synchronized void connectForStreams(Collection<String> streams) {
        try {
            String streamsPart = streams.stream()
                    .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                    .collect(Collectors.joining("/"));
            String url = "wss://stream.binance.com:9443/stream?streams=" + streamsPart;

            log.info("Connecting to Binance WS ({} streams)...", streams.size());
            HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(url), new Listener())
                    .thenAccept(ws -> {
                        webSocketRef.set(ws);
                        lastMessageAt = System.currentTimeMillis();
                        log.info("Binance WS connected.");
                    })
                    .exceptionally(ex -> {
                        log.error("Failed to connect WS: {}", ex.getMessage(), ex);
                        return null;
                    });
        } catch (Exception e) {
            log.error("WS connect error: {}", e.getMessage(), e);
        }
    }

    public boolean isAlive(long staleThresholdMs) {
        WebSocket ws = webSocketRef.get();
        long age = System.currentTimeMillis() - lastMessageAt;
        return ws != null && age < staleThresholdMs;
    }

    public synchronized void reconnect() {
        try {
            WebSocket ws = webSocketRef.getAndSet(null);
            if (ws != null) { try { ws.abort(); } catch (Exception ignore) {} }
        } catch (Exception ignore) {}
        // Няма запазен списък с потоци тук — оставяме сервиса да извика отново connectForStreams(...)
        log.warn("WS aborted. You should call connectForStreams(...) again from the service.");
    }

    public Flow.Publisher<String> getPublisher() {
        return publisher;
    }

    private final class Listener implements WebSocket.Listener {
        private final StringBuilder textBuffer = new StringBuilder();

        @Override public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
            log.info("WS opened.");
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            try {
                lastMessageAt = System.currentTimeMillis();
                textBuffer.append(data);
                if (last) {
                    String full = textBuffer.toString();
                    textBuffer.setLength(0);
                    publisher.submit(full); // публикуваме само завършен JSON
                }
            } catch (Exception e) {
                log.warn("WS onText error: {}", e.getMessage(), e);
                textBuffer.setLength(0);
            } finally {
                webSocket.request(1);
            }
            return null;
        }

        @Override public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            webSocket.request(1);
            return null;
        }

        @Override public void onError(WebSocket webSocket, Throwable error) {
            log.error("Binance WebSocket error: {}", error.getMessage(), error);
        }

        @Override public CompletionStage<?> onClose(WebSocket ws, int code, String reason) {
            log.warn("WS closed ({}): {}.", code, reason);
            return null;
        }
    }
}
