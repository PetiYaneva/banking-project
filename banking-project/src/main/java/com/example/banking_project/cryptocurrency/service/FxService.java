package com.example.banking_project.cryptocurrency.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FxService  {

    // конфигурируем параметър (примерно 1 USD = 1.80 BGN). Поддържай актуален!
    @Value("${fx.usd_bgn_rate:1.80}")
    private BigDecimal usdToBgn;

    public BigDecimal usdToBgn(BigDecimal usd) {
        if (usd == null) return BigDecimal.ZERO;
        return usd.multiply(usdToBgn).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getUsdToBgnRate() {
        return usdToBgn;
    }
}
