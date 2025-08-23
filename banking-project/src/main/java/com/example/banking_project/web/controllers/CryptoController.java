package com.example.banking_project.web.controllers;

import com.example.banking_project.cryptocurrency.service.CryptoService;
import com.example.banking_project.web.dto.crypto.CryptoHistoryDto;
import com.example.banking_project.web.dto.crypto.CryptoPriceDto;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final CryptoService service;
    public CryptoController(CryptoService service) { this.service = service; }

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping(value = "/simple-price", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<CryptoPriceDto>> getSimplePrice(
            @RequestParam @NotBlank String ids,
            @RequestParam(name = "vs") @NotBlank String vsCurrencies
    ) {
        return service.getSimplePrices(ids, vsCurrencies);
    }

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CryptoHistoryDto> getHistory(
            @RequestParam @NotBlank String id,
            @RequestParam(name = "vs", defaultValue = "usd") String vsCurrency,
            @RequestParam(defaultValue = "30") String days
    ) {
        return service.getHistory(id, vsCurrency, days);
    }
}
