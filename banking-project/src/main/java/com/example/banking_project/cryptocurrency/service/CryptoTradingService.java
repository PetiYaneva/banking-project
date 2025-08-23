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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
        // 0) Normalization & validation
        String asset = Objects.requireNonNull(req.asset(), "asset is required").toLowerCase(Locale.ROOT);
        if (req.quantity() == null || req.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        OrderSide side = Objects.requireNonNull(req.side(), "side is required");

        // 1) Account & ownership
        Account account = accountService.getAccountByIban(req.iban());
        if (account == null) throw new IllegalArgumentException("Account not found for IBAN");
        if (!Objects.equals(account.getUser().getId(), req.userId())) {
            throw new IllegalArgumentException("IBAN does not belong to user");
        }
        if (!Boolean.TRUE.equals(account.getCryptoEnabled())) {
            throw new IllegalStateException("Crypto trading disabled for this account");
        }

        // 2) Fiat currency & live price (single reactive call)
        String fiat = Optional.ofNullable(account.getCurrencyCode()).filter(s -> !s.isBlank()).orElse("BGN");
        String vs = fiat.toLowerCase(Locale.ROOT);

        BigDecimal price = cryptoService.getSimplePrices(asset, vs)
                .blockOptional().orElse(List.of()).stream()
                .filter(p -> p.id().equals(asset))
                .findFirst()
                .map(p -> p.prices().get(vs))
                .orElseThrow(() -> new IllegalStateException("Price not available for " + asset));

        // 3) Amounts (use consistent scales)
        BigDecimal gross = price.multiply(req.quantity());
        BigDecimal fee   = gross.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netBuy  = gross.add(fee).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netSell = gross.subtract(fee).setScale(2, RoundingMode.HALF_UP);

        // 4) Balance & Income/Expense
        if (side == OrderSide.BUY) {
            // optional explicit check (ако debitByIban вече проверява, можеш да пропуснеш)
            // BigDecimal balance = accountService.getBalanceByIban(req.iban());
            // if (balance.compareTo(netBuy) < 0) throw new IllegalArgumentException("Insufficient funds");
            accountService.debitByIban(req.iban(), netBuy);
            transactionService.recordExpense(
                    req.userId(), req.iban(), netBuy,
                    "CRYPTO BUY " + asset.toUpperCase() + " @ " + price + " (fee " + fee + " " + fiat + ")"
            );
        } else {
            CryptoHolding exists = holdingRepo.findByAccountIdAndAsset(account.getId(), asset)
                    .orElseThrow(() -> new IllegalArgumentException("No holdings for SELL"));
            if (exists.getQuantity().compareTo(req.quantity()) < 0) {
                throw new IllegalArgumentException("Not enough asset to sell");
            }
            accountService.creditByIban(req.iban(), netSell);
            transactionService.recordIncome(
                    req.userId(), req.iban(), netSell,
                    "CRYPTO SELL " + asset.toUpperCase() + " @ " + price + " (fee " + fee + " " + fiat + ")"
            );
        }

        // 5) Update holdings (qty scale 8, price scale 2)
        CryptoHolding holding = holdingRepo.findByAccountIdAndAsset(account.getId(), asset)
                .orElseGet(() -> {
                    CryptoHolding h = new CryptoHolding();
                    h.setAccount(account);
                    h.setAsset(asset);
                    h.setQuantity(BigDecimal.ZERO.setScale(8, RoundingMode.HALF_UP));
                    h.setAvgPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                    h.setFiatCurrency(fiat);
                    return h;
                });

        if (side == OrderSide.BUY) {
            BigDecimal oldQty  = holding.getQuantity().setScale(8, RoundingMode.HALF_UP);
            BigDecimal newQty  = oldQty.add(req.quantity().setScale(8, RoundingMode.HALF_UP));
            BigDecimal oldCost = holding.getAvgPrice().setScale(2, RoundingMode.HALF_UP).multiply(oldQty);
            BigDecimal newCost = oldCost.add(gross.setScale(2, RoundingMode.HALF_UP));
            BigDecimal newAvg  = newQty.signum() == 0
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : newCost.divide(newQty, 8, RoundingMode.HALF_UP); // пазим 8 за avg per unit
            holding.setQuantity(newQty);
            holding.setAvgPrice(newAvg);
        } else {
            BigDecimal newQty = holding.getQuantity().setScale(8, RoundingMode.HALF_UP)
                    .subtract(req.quantity().setScale(8, RoundingMode.HALF_UP));
            holding.setQuantity(newQty);
            if (newQty.signum() == 0) {
                holding.setAvgPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
        }
        holdingRepo.save(holding);

        // 6) Save order
        CryptoOrder order = new CryptoOrder();
        order.setUserId(req.userId());
        order.setAccount(account);
        order.setIban(req.iban());
        order.setAsset(asset);
        order.setSide(side);
        order.setQuantity(req.quantity().setScale(8, RoundingMode.HALF_UP));
        order.setPrice(price.setScale(8, RoundingMode.HALF_UP));
        order.setGrossAmount(gross.setScale(2, RoundingMode.HALF_UP));
        order.setFeeAmount(fee);
        order.setNetAmount((side == OrderSide.BUY ? netBuy : netSell));
        order.setStatus(OrderStatus.FILLED);
        order.setFiatCurrency(fiat);
        order.setExecutedAt(Instant.now());
        orderRepo.save(order);

        return new PlaceOrderResponse(
                order.getId(), order.getStatus(), order.getPrice(),
                order.getGrossAmount(), order.getFeeAmount(), order.getNetAmount(), fiat
        );
    }


    @Transactional(readOnly = true)
    public List<HoldingView> getPortfolioByAccount(UUID accountId, String vsCurrency) {
        var holdings = holdingRepo.findByAccountId(accountId);
        if (holdings.isEmpty()) return List.of();

        String idsCsv = holdings.stream()
                .map(h -> h.getAsset().toLowerCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.joining(","));

        Map<String, BigDecimal> market = new HashMap<>();
        cryptoService.getSimplePrices(idsCsv, vsCurrency.toLowerCase(Locale.ROOT))
                .blockOptional().orElse(List.of())
                .forEach(p -> market.put(p.id(), p.prices().get(vsCurrency.toLowerCase(Locale.ROOT))));

        String iban = holdings.get(0).getAccount().getIban(); // всички са от един акаунт
        return holdings.stream().map(h -> {
            String a = h.getAsset().toLowerCase(Locale.ROOT);
            BigDecimal mp = market.getOrDefault(a, BigDecimal.ZERO).setScale(8, RoundingMode.HALF_UP);
            BigDecimal qty = h.getQuantity().setScale(8, RoundingMode.HALF_UP);
            BigDecimal mv = mp.multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pnl = mp.subtract(h.getAvgPrice()).multiply(qty).setScale(2, RoundingMode.HALF_UP);
            return new HoldingView(accountId, iban, a, qty, h.getAvgPrice(), mp, mv, pnl);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<CryptoOrder> getOrdersByAccount(UUID accountId) {
        return orderRepo.findByAccountIdOrderByExecutedAtDesc(accountId);
    }

    public void assertAccountOwnedByUserOrAdmin(UUID accountId, UUID userId) {
        if (isAdmin()) return;
        Account acc = accountService.getAccountById(accountId);
        if (acc == null || acc.getUser() == null || !acc.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: account does not belong to current user");
        }
    }

    public void assertAccountOwnedByUserOrAdminByIban(String iban, UUID userId) {
        if (isAdmin()) return;
        Account acc = accountService.getAccountByIban(iban);
        if (acc == null || acc.getUser() == null || !acc.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: IBAN does not belong to current user");
        }
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

}
