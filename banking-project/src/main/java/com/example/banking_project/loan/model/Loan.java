package com.example.banking_project.loan.model;

import com.example.banking_project.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "date_of_applying", nullable = false)
    private LocalDate dateOfApplying;

    @Column(name = "final_date", nullable = false)
    private LocalDate finalDate;

    @Column(name = "next_date_of_payment", nullable = false)
    private LocalDate nextDateOfPayment;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "remaining_amount", nullable = false)
    private BigDecimal remainingAmount;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;  // Лихвен процент

    @Column(name = "missed_payments")
    private int missedPayments;

    @Column(name = "monthly_payment", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_status", nullable = false)
    private LoanStatus loanStatus;  // Статус на кредита

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
