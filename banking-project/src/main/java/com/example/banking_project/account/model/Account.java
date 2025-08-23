package com.example.banking_project.account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import com.example.banking_project.user.model.User;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "balance")
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "iban", unique = true)
    private String iban;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "crypto_enabled", nullable = false)
    private Boolean cryptoEnabled = Boolean.TRUE;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
