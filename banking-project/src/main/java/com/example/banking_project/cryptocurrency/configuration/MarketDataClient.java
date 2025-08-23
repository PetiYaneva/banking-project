package com.example.banking_project.cryptocurrency.configuration;

import com.example.banking_project.web.dto.crypto.CryptoHistoryDto;
import com.example.banking_project.web.dto.crypto.CryptoPriceDto;
import reactor.core.publisher.Mono;
import java.util.List;

public interface MarketDataClient {
    Mono<List<CryptoPriceDto>> simplePrice(String idsCsv, String vsCurrenciesCsv);
    Mono<CryptoHistoryDto> history(String id, String vsCurrency, String days);
}
