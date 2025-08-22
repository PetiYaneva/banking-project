package com.example.banking_project.web.controllers;
import com.example.banking_project.cryptocurrency.CryptoService;
import com.example.banking_project.web.dto.crypto.CryptoHistoryDto;
import com.example.banking_project.web.dto.crypto.CryptoPriceDto;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final CryptoService service;
    public CryptoController(CryptoService service) { this.service = service; }

    @GetMapping(value = "/simple-price", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<CryptoPriceDto>> getSimplePrice(
            @RequestParam @NotBlank String ids,          // e.g. bitcoin,ethereum
            @RequestParam(name = "vs") @NotBlank String vsCurrencies // e.g. usd,eur,bgn
    ) {
        return service.getSimplePrices(ids, vsCurrencies);
    }

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CryptoHistoryDto> getHistory(
            @RequestParam @NotBlank String id,           // e.g. bitcoin
            @RequestParam(name = "vs", defaultValue = "usd") String vsCurrency,
            @RequestParam(defaultValue = "30") String days
    ) {
        return service.getHistory(id, vsCurrency, days);
    }
}
