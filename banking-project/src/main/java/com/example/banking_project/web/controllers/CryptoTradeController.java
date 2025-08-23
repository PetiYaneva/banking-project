package com.example.banking_project.web.controllers;

import com.example.banking_project.cryptocurrency.model.CryptoOrder;
import com.example.banking_project.cryptocurrency.service.CryptoTradingService;
import com.example.banking_project.web.dto.crypto.HoldingView;
import com.example.banking_project.web.dto.crypto.PlaceOrderRequest;
import com.example.banking_project.web.dto.crypto.PlaceOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/crypto/trade")
@RequiredArgsConstructor
public class CryptoTradeController {

    private final CryptoTradingService tradingService;

    @PostMapping("/order")
    public PlaceOrderResponse placeOrder(@RequestBody @Valid PlaceOrderRequest req) {
        return tradingService.placeMarketOrder(req);
    }

    @GetMapping("/portfolio/by-account")
    public List<HoldingView> portfolioByAccount(@RequestParam UUID accountId,
                                                @RequestParam(defaultValue = "usd") String vs) {
        return tradingService.getPortfolioByAccount(accountId, vs);
    }

    @GetMapping("/orders/by-account")
    public List<CryptoOrder> ordersByAccount(@RequestParam UUID accountId) {
        return tradingService.getOrdersByAccount(accountId);
    }
}
