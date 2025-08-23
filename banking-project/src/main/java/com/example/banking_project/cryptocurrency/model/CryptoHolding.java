package com.example.banking_project.cryptocurrency.model;

import com.example.banking_project.account.model.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "crypto_holding",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "asset"}))
public class CryptoHolding {
    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private String asset; // "bitcoin", "ethereum"

    @Column(nullable = false, precision = 21, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 21, scale = 8)
    private BigDecimal avgPrice;

    @Column(nullable = false)
    private String fiatCurrency; // напр. "BGN" / "EUR"

    @Version
    private long version;

}
