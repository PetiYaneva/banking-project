package com.example.banking_project.transaction.model;

import com.example.banking_project.account.model.Account;
import jakarta.persistence.*;
import lombok.*;
import com.example.banking_project.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

@Builder
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    private TransactionStatus transactionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "sender", nullable = false)
    private String sender;

    @Column(name = "receiver", nullable = false)
    private String receiver;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_on", nullable = false)
    private LocalDate createdOn;

    @Column(name = "currency", nullable = false)
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    private Income income;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    private Expense expense;

}
