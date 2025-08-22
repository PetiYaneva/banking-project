package com.example.banking_project.cryptocurrency;

import com.example.banking_project.cryptocurrency.configuration.MarketDataClient;
import com.example.banking_project.web.dto.crypto.CryptoHistoryDto;
import com.example.banking_project.web.dto.crypto.CryptoPriceDto;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CryptoService {

    private final MarketDataClient client;

    public CryptoService(MarketDataClient client) {
        this.client = client;
    }

    @Retry(name = "marketData")
    @Cacheable(cacheNames = "crypto-simple-price", key = "#idsCsv + '|' + #vsCurrenciesCsv")
    public Mono<List<CryptoPriceDto>> getSimplePrices(String idsCsv, String vsCurrenciesCsv) {
        return client.simplePrice(idsCsv, vsCurrenciesCsv);
    }

    @Retry(name = "marketData")
    @Cacheable(cacheNames = "crypto-history", key = "#id + '|' + #vsCurrency + '|' + #days")
    public Mono<CryptoHistoryDto> getHistory(String id, String vsCurrency, String days) {
        return client.history(id, vsCurrency, days);
    }
}
