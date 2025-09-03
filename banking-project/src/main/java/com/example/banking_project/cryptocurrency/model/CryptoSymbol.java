package com.example.banking_project.cryptocurrency.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "crypto_symbol")
@Getter
@Setter
public class CryptoSymbol {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable=false, unique=true, length=16) private String symbol;
    @Column(nullable=false, unique=true, length=32) private String binancePair;
    @Column(nullable=false, length=64) private String coingeckoId;
    @Column(nullable=false) private boolean enabled = true;
    @Column(nullable=false) private OffsetDateTime updatedAt;
    @PrePersist @PreUpdate void touch(){ updatedAt = OffsetDateTime.now(); }
}
