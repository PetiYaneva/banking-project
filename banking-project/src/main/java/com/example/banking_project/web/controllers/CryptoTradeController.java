package com.example.banking_project.web.controllers;

import com.example.banking_project.cryptocurrency.model.CryptoOrder;
import com.example.banking_project.cryptocurrency.service.CryptoTradingService;
import com.example.banking_project.web.dto.crypto.HoldingView;
import com.example.banking_project.web.dto.crypto.PlaceOrderRequest;
import com.example.banking_project.web.dto.crypto.PlaceOrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/crypto/trade")
@RequiredArgsConstructor
public class CryptoTradeController {

    private final CryptoTradingService tradingService;

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @PostMapping("/order")
    public PlaceOrderResponse placeOrder(@RequestBody @Valid PlaceOrderRequest req,
                                         HttpServletRequest request) {
        UUID userId = UUID.fromString((String) request.getAttribute("userId"));

        tradingService.assertAccountOwnedByUserOrAdminByIban(req.iban(), userId);

        return tradingService.placeMarketOrder(req.withUserId(userId));
    }

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping("/portfolio/by-account")
    public List<HoldingView> portfolioByAccount(@RequestParam UUID accountId,
                                                @RequestParam(defaultValue = "usd") String vs,
                                                HttpServletRequest request) {
        UUID userId = UUID.fromString((String) request.getAttribute("userId"));
        tradingService.assertAccountOwnedByUserOrAdmin(accountId, userId);
        return tradingService.getPortfolioByAccount(accountId, vs);
    }

    @PreAuthorize("hasAuthority('PROFILE_COMPLETED') and hasAnyRole('USER','ADMIN')")
    @GetMapping("/orders/by-account")
    public List<CryptoOrder> ordersByAccount(@RequestParam UUID accountId,
                                             HttpServletRequest request) {
        UUID userId = UUID.fromString((String) request.getAttribute("userId"));
        tradingService.assertAccountOwnedByUserOrAdmin(accountId, userId);
        return tradingService.getOrdersByAccount(accountId);
    }
}
