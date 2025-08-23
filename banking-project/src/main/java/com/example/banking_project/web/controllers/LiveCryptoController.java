package com.example.banking_project.web.controllers;

import com.example.banking_project.cryptocurrency.service.LivePriceService;
import com.example.banking_project.web.dto.crypto.Ticker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class LiveCryptoController {

    private final LivePriceService service;

    /**
     * Server-Sent Events: /api/crypto/live
     * – изисква логин + PROFILE_COMPLETED (USER/ADMIN)
     * – с лека „throttle“: максимум 1 събитие/сек.
     */
    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping(value = "/api/crypto/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<Ticker>> streamBtcLive() {
        Flux<Ticker> stream = service.streamPrices()
                .sample(Duration.ofSeconds(1))
                .onBackpressureLatest();

        return ResponseEntity
                .ok()
                .header("Cache-Control", "no-store")
                .body(stream);
    }
}
