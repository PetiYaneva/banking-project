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
import java.util.Locale;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final CryptoService service;
    public CryptoController(CryptoService service) { this.service = service; }

    // достъпът се управлява от SecurityConfiguration -> permitAll за GET /simple-price
    @PreAuthorize("permitAll()")
    @GetMapping(value = "/simple-price", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<CryptoPriceDto>> getSimplePrice(
            @RequestParam @NotBlank String ids,
            @RequestParam(name = "vs") @NotBlank String vsCurrencies
    ) {
        // нормализация
        String normIds = ids.toLowerCase(Locale.ROOT).trim();
        String normVs  = vsCurrencies.toLowerCase(Locale.ROOT).trim();
        return service.getSimplePrices(normIds, normVs);
    }

    // достъпът се управлява от SecurityConfiguration -> permitAll за GET /history
    @PreAuthorize("permitAll()")
    @GetMapping(value = "/history/{id}/{vs}/{days}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CryptoHistoryDto> getHistory(
            @PathVariable("id") @NotBlank String id,
            @PathVariable("vs") String vsCurrency,
            @PathVariable("days") String days
    ) {
        String normId = id.toLowerCase(Locale.ROOT).trim();
        String normVs = (vsCurrency == null || vsCurrency.isBlank() ? "usd" : vsCurrency.toLowerCase(Locale.ROOT).trim());
        String normDays = (days == null || days.isBlank() ? "30" : days.trim());

        return service.getHistory(normId, normVs, normDays);
    }
}
