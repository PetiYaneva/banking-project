package com.example.banking_project.web.controllers;
import com.example.banking_project.cryptocurrency.service.LivePriceService;
import com.example.banking_project.web.dto.crypto.Ticker;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class LiveCryptoController {

    private final LivePriceService service;

    public LiveCryptoController(LivePriceService service) {
        this.service = service;
    }

    /**
     * Server-Sent Events endpoint: /api/crypto/live
     * Stream Live BTC prices every second
     */
    @GetMapping(value = "/api/crypto/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Ticker> streamBtcLive() {
        return service.streamPrices().sample(Duration.ofSeconds(1));
    }
}