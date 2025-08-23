package com.example.banking_project.cryptocurrency.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.service.AccountService;
import com.example.banking_project.cryptocurrency.model.CryptoHolding;
import com.example.banking_project.cryptocurrency.model.CryptoOrder;
import com.example.banking_project.cryptocurrency.model.OrderSide;
import com.example.banking_project.cryptocurrency.model.OrderStatus;
import com.example.banking_project.cryptocurrency.repository.CryptoHoldingRepository;
import com.example.banking_project.cryptocurrency.repository.CryptoOrderRepository;
import com.example.banking_project.transaction.service.TransactionService;
import com.example.banking_project.web.dto.crypto.HoldingView;
import com.example.banking_project.web.dto.crypto.PlaceOrderRequest;
import com.example.banking_project.web.dto.crypto.PlaceOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CryptoTradingService {

    private final CryptoOrderRepository orderRepo;
    private final CryptoHoldingRepository holdingRepo;

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CryptoService cryptoService;

    // по желание: изнеси в application.yml и инжектирай с @Value
    private static final BigDecimal FEE_RATE = new BigDecimal("0.003"); // 0.3%

    @Transactional
    public PlaceOrderResponse placeMarketOrder(PlaceOrderRequest req) {
        // 1) сметка и собственост
        Account account = accountService.getAccountByIban(req.iban());
        if (!account.getUser().getId().equals(req.userId())) {
            throw new IllegalArgumentException("IBAN does not belong to user");
        }
        if (!Boolean.TRUE.equals(account.getCryptoEnabled())) {
            throw new IllegalStateException("Crypto trading disabled for this account");
        }

        String fiat = Optional.ofNullable(account.getCurrencyCode())
                .filter(s -> !s.isBlank())
                .orElse("BGN");
        String vs = fiat.toLowerCase(Locale.ROOT);

        BigDecimal price = cryptoService.getSimplePrices(req.asset(), vs)
                .blockOptional().orElse(List.of()).stream()
                .filter(p -> p.id().equals(req.asset()))
                .findFirst()
                .map(p -> p.prices().get(vs))
                .orElseThrow(() -> new IllegalStateException("Price not available for " + req.asset()));

        var priceMap = cryptoService.getSimplePrices(req.asset(), vs)
                .blockOptional().orElse(List.of()).stream()
                .filter(p -> p.id().equals(req.asset()))
                .findFirst()
                .map(p -> p.prices().get(vs))
                .orElseThrow(() -> new IllegalStateException("Price not available for " + req.asset()));

        BigDecimal gross = price.multiply(req.quantity());
        BigDecimal fee   = gross.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net   = (req.side() == OrderSide.BUY) ? gross.add(fee) : gross.subtract(fee);

        // 3) движение по сметката + Income/Expense с ясно описание
        if (req.side() == OrderSide.BUY) {
            accountService.debitByIban(req.iban(), net);
            transactionService.recordExpense(req.userId(), req.iban(), net,
                    "Crypto BUY " + req.asset().toUpperCase() + " @ " + price);
        } else {
            // проверка за наличност преди кредит
            var exists = holdingRepo.findByAccountIdAndAsset(account.getId(), req.asset())
                    .orElseThrow(() -> new IllegalArgumentException("No holdings for SELL"));
            if (exists.getQuantity().compareTo(req.quantity()) < 0) {
                throw new IllegalArgumentException("Not enough asset to sell");
            }
            accountService.creditByIban(req.iban(), net);
            transactionService.recordIncome(req.userId(), req.iban(), net,
                    "Crypto SELL " + req.asset().toUpperCase() + " @ " + price);
        }

        // 4) обнови портфейла (средна себестойност към account)
        CryptoHolding holding = holdingRepo.findByAccountIdAndAsset(account.getId(), req.asset())
                .orElseGet(() -> {
                    CryptoHolding h = new CryptoHolding();
                    h.setAccount(account);
                    h.setAsset(req.asset());
                    h.setQuantity(BigDecimal.ZERO);
                    h.setAvgPrice(BigDecimal.ZERO);
                    h.setFiatCurrency(fiat);
                    return h;
                });

        if (req.side() == OrderSide.BUY) {
            BigDecimal newQty  = holding.getQuantity().add(req.quantity());
            BigDecimal newCost = holding.getAvgPrice().multiply(holding.getQuantity()).add(gross); // avg*qty + gross
            BigDecimal newAvg  = newQty.signum()==0 ? BigDecimal.ZERO
                    : newCost.divide(newQty, 8, RoundingMode.HALF_UP);
            holding.setQuantity(newQty);
            holding.setAvgPrice(newAvg);
        } else {
            // SELL: намаляваме qty; avgPrice остава за остатъка
            holding.setQuantity(holding.getQuantity().subtract(req.quantity()));
            if (holding.getQuantity().signum()==0) holding.setAvgPrice(BigDecimal.ZERO);
        }
        holdingRepo.save(holding);

        // 5) запиши ордера
        CryptoOrder order = new CryptoOrder();
        order.setUserId(req.userId());
        order.setAccount(account);
        order.setIban(req.iban());
        order.setAsset(req.asset());
        order.setSide(req.side());
        order.setQuantity(req.quantity());
        order.setPrice(price);
        order.setGrossAmount(gross.setScale(2, RoundingMode.HALF_UP));
        order.setFeeAmount(fee);
        order.setNetAmount(net.setScale(2, RoundingMode.HALF_UP));
        order.setStatus(OrderStatus.FILLED);
        order.setFiatCurrency(fiat);
        order.setExecutedAt(Instant.now());
        orderRepo.save(order);

        return new PlaceOrderResponse(
                order.getId(), order.getStatus(), price,
                order.getGrossAmount(), order.getFeeAmount(), order.getNetAmount(), fiat
        );
    }

    @Transactional(readOnly = true)
    public List<HoldingView> getPortfolioByAccount(UUID accountId, String vsCurrency) {
        var holdings = holdingRepo.findByAccountId(accountId);
        if (holdings.isEmpty()) return List.of();

        var account = holdings.get(0).getAccount(); // всички са към един акаунт
        String idsCsv = holdings.stream().map(CryptoHolding::getAsset).distinct().collect(Collectors.joining(","));

        Map<String, BigDecimal> market = new HashMap<>();
        if (!idsCsv.isBlank()) {
            cryptoService.getSimplePrices(idsCsv, vsCurrency).blockOptional().orElse(List.of())
                    .forEach(p -> market.put(p.id(), p.prices().get(vsCurrency)));
        }

        String iban = account.getIban();
        return holdings.stream().map(h -> {
            BigDecimal mp = market.getOrDefault(h.getAsset(), BigDecimal.ZERO);
            BigDecimal mv = mp.multiply(h.getQuantity());
            BigDecimal pnl = mp.subtract(h.getAvgPrice()).multiply(h.getQuantity());
            return new HoldingView(accountId, iban, h.getAsset(), h.getQuantity(), h.getAvgPrice(), mp, mv, pnl);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<CryptoOrder> getOrdersByAccount(UUID accountId) {
        return orderRepo.findByAccountIdOrderByExecutedAtDesc(accountId);
    }
}
