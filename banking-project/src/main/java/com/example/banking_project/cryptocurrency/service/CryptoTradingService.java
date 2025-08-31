package com.example.banking_project.cryptocurrency.service;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.service.AccountService;
import com.example.banking_project.cryptocurrency.model.CryptoHolding;
import com.example.banking_project.cryptocurrency.model.CryptoOrder;
import com.example.banking_project.cryptocurrency.model.OrderSide;
import com.example.banking_project.cryptocurrency.repository.CryptoHoldingRepository;
import com.example.banking_project.cryptocurrency.repository.CryptoOrderRepository;
import com.example.banking_project.transaction.model.TransactionType;
import com.example.banking_project.transaction.service.TransactionService;
import com.example.banking_project.user.service.UserService;
import com.example.banking_project.web.dto.TransactionRequest;
import com.example.banking_project.web.dto.crypto.HoldingView;
import com.example.banking_project.web.dto.crypto.PlaceOrderRequest;
import com.example.banking_project.web.dto.crypto.PlaceOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoTradingService {

    private final AccountService accountsService;
    private final TransactionService transactionService;
    private final CryptoPriceService priceService;
    private final CryptoHoldingRepository holdingRepository;
    private final CryptoOrderRepository orderRepository;
    private final CryptoService cryptoService;
    private final FxService fxService;
    private final UserService userService;

    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest req, UUID userId) {
        Account account = accountsService.getAccountByIban(req.iban());
        String symbol = req.symbol();

        BigDecimal unitUsd = Optional.ofNullable(priceService.getLiveUsdPrice(symbol))
                .orElseThrow(() -> new IllegalStateException("Няма live USD цена за " + symbol));

        BigDecimal usdToBgn = fxService.getUsdToBgnRate();

        BigDecimal unitBgn = unitUsd.multiply(usdToBgn).setScale(6, RoundingMode.HALF_UP);

        BigDecimal totalCostBgn = unitBgn.multiply(req.quantity()).setScale(2, RoundingMode.HALF_UP);

        if (req.side() == OrderSide.BUY) {
            BigDecimal balance = account.getBalance();

            if (balance == null || balance.compareTo(totalCostBgn) < 0) {
                throw new IllegalArgumentException("Недостатъчна наличност по избраната сметка.");
            }

            String desc = "Crypto BUY " + symbol + " x " + req.quantity() + " @ " + unitUsd + " USD";
            transactionService.createTransaction(TransactionRequest.builder()
                    .account(account)
                    .transactionType(TransactionType.WITHDRAWAL)
                    .currency(Currency.getInstance("BGN"))
                    .iban(account.getIban())
                    .amount(totalCostBgn)
                    .description(desc)
                    .userId(req.userId())
                    .build());

            CryptoHolding holding = holdingRepository.findByAccount_IdAndAsset(account.getId(), symbol)
                    .orElseGet(() -> {
                        CryptoHolding h = new CryptoHolding();
                        h.setAccount(account);
                        h.setIban(account.getIban());
                        h.setAsset(symbol);
                        h.setQuantity(BigDecimal.ZERO);
                        h.setAvgPrice(BigDecimal.ZERO);
                        h.setFiatCurrency(account.getCurrencyCode() != null ? account.getCurrencyCode() : "BGN");
                        h.setUpdatedAt(OffsetDateTime.now());
                        return h;
                    });

            BigDecimal oldQty = holding.getQuantity();
            BigDecimal oldAvg = holding.getAvgPrice();
            BigDecimal newQty = oldQty.add(req.quantity());

            BigDecimal newAvg = (oldQty.multiply(oldAvg).add(req.quantity().multiply(unitBgn)))
                    .divide(newQty, 8, RoundingMode.HALF_UP);

            holding.setQuantity(newQty);
            holding.setAvgPrice(newAvg);
            holding.setFiatCurrency(account.getCurrencyCode() != null ? account.getCurrencyCode() : "BGN");
            holding.setIban(account.getIban());
            holding.setUpdatedAt(OffsetDateTime.now());
            holding.setUser(userService.findUserById(userId));
            holdingRepository.save(holding);

        } else if (req.side() == OrderSide.SELL) {

            CryptoHolding holding = holdingRepository.findByAccount_IdAndAsset(account.getId(), symbol)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Няма наличност от " + symbol + " за продажба."));

            BigDecimal totalProceedsBgn = unitBgn.multiply(req.quantity()).setScale(2, RoundingMode.HALF_UP);

            String desc = "Crypto SELL " + symbol + " x " + req.quantity() + " @ " + unitUsd + " USD";
            transactionService.createTransaction(TransactionRequest.builder()
                    .account(account)
                    .transactionType(TransactionType.DEPOSIT)
                    .currency(Currency.getInstance("BGN"))
                    .iban(account.getIban())
                    .amount(totalProceedsBgn)
                    .description(desc)
                    .userId(userId)
                    .build());

            BigDecimal oldQty = holding.getQuantity();
            BigDecimal newQty = oldQty.subtract(req.quantity());
            holding.setQuantity(newQty);

            if (newQty.signum() == 0) {
                holding.setAvgPrice(BigDecimal.ZERO);
            }
            holding.setIban(account.getIban());
            holding.setFiatCurrency(account.getCurrencyCode() != null ? account.getCurrencyCode() : "BGN");
            holding.setUpdatedAt(OffsetDateTime.now());
            holding.setUser(userService.findUserById(userId));
            holdingRepository.save(holding);

            totalCostBgn = totalProceedsBgn;
        }

        return new PlaceOrderResponse(
                account.getIban(),
                symbol,
                req.side().name(),
                req.quantity(),
                unitUsd.setScale(6, RoundingMode.HALF_UP),
                unitBgn,
                totalCostBgn,
                null
        );
    }

    public List<HoldingView> getPortfolioByUser(UUID userId, String vsCurrency) {
        var holdings = holdingRepository.findAllByUserId(userId);

        if (holdings.isEmpty()) {
            log.info("No crypto holdings for user {}", userId);
            return List.of();
        }

        String idsCsv = holdings.stream()
                .map(h -> h.getAsset().toLowerCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.joining(","));

        Map<String, BigDecimal> market = new HashMap<>();
        cryptoService.getSimplePrices(idsCsv, vsCurrency.toLowerCase(Locale.ROOT))
                .blockOptional().orElse(List.of())
                .forEach(p -> market.put(p.id(), p.prices().get(vsCurrency.toLowerCase(Locale.ROOT))));

        return holdings.stream().map(h -> {
            String symbol = h.getAsset().toLowerCase(Locale.ROOT);
            BigDecimal mp = market.getOrDefault(symbol, BigDecimal.ZERO).setScale(8, RoundingMode.HALF_UP);
            BigDecimal qty = h.getQuantity().setScale(8, RoundingMode.HALF_UP);
            BigDecimal mv = mp.multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pnl = mp.subtract(h.getAvgPrice()).multiply(qty).setScale(2, RoundingMode.HALF_UP);

            return new HoldingView(
                    h.getAccount().getId(),
                    h.getAccount().getIban(),
                    h.getAsset(),
                    qty,
                    h.getAvgPrice(),
                    mp,
                    mv,
                    pnl
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<CryptoOrder> getOrdersByAccount(UUID accountId, UUID userId) {
        assertAccountOwnedByUserOrAdmin(accountId, userId);
        return orderRepository.findByAccountIdOrderByExecutedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<CryptoOrder> getOrdersByUser(UUID userId) {
        if (isAdmin()) {
            return orderRepository.findAllByOrderByExecutedAtDesc();
        } else {
            return orderRepository.findByAccount_User_IdOrderByExecutedAtDesc(userId);
        }
    }

    public void assertAccountOwnedByUserOrAdmin(UUID accountId, UUID userId) {
        if (isAdmin()) return;
        Account acc = accountsService.getAccountById(accountId);
        if (acc == null || acc.getUser() == null || !acc.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: account does not belong to current user");
        }
    }

    public void assertAccountOwnedByUserOrAdminByIban(String iban, UUID userId) {
        if (isAdmin()) return;
        Account acc = accountsService.getAccountByIban(iban);
        if (acc == null || acc.getUser() == null || !acc.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: IBAN does not belong to current user");
        }
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
