package com.example.banking_project.cryptocurrency.model;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "crypto_holding",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"account_id", "asset"})
        }
)
public class CryptoHolding {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "iban", length = 34)
    private String iban;

    @Column(nullable = false)
    private String asset;

    @Column(nullable = false, precision = 21, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 21, scale = 8)
    private BigDecimal avgPrice;

    @Column(nullable = false, length = 8)
    private String fiatCurrency;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private long version;
}
