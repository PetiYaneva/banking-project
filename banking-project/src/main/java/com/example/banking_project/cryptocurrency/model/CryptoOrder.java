package com.example.banking_project.cryptocurrency.model;

import com.example.banking_project.account.model.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "crypto_order")
public class CryptoOrder {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false) private UUID userId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false) private String iban;
    @Column(nullable = false) private String asset;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private OrderSide side;

    @Column(nullable = false, precision = 21, scale = 8) private BigDecimal quantity;
    @Column(nullable = false, precision = 21, scale = 8) private BigDecimal price;

    @Column(nullable = false, precision = 21, scale = 2) private BigDecimal grossAmount;
    @Column(nullable = false, precision = 21, scale = 2) private BigDecimal feeAmount;
    @Column(nullable = false, precision = 21, scale = 2) private BigDecimal netAmount;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false) private String fiatCurrency;
    @Column(nullable = false) private Instant executedAt;

    @Version
    private long version;

}

