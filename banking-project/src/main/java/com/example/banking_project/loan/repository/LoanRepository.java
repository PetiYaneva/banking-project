package com.example.banking_project.loan.repository;

import com.example.banking_project.loan.model.Loan;
import com.example.banking_project.loan.view.CreditHistoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    @Query(value = """
    SELECT
        l.user_id AS userId,
        COUNT(*) AS totalLoans,
        SUM(CASE WHEN l.loan_status = 'OVERDUE' OR l.missed_payments > 0 THEN 1 ELSE 0 END) AS overdueLoans,
        CASE
            WHEN COUNT(*) = 0 THEN 'neutral'
            WHEN SUM(CASE WHEN l.loan_status = 'OVERDUE' OR l.missed_payments > 0 THEN 1 ELSE 0 END) > 0 THEN 'bad'
            ELSE 'good'
        END AS creditStatus,
        MAX(l.date_of_applying) AS lastLoanDate
    FROM loans l
    WHERE l.user_id = :userId
    GROUP BY l.user_id
""", nativeQuery = true)
    CreditHistoryView getCreditHistoryByUserId(@Param("userId") UUID userId);

    @Query(value = """
    SELECT COALESCE(SUM(l.monthly_payment), 0)
    FROM loans l
    WHERE l.user_id = :userId
      AND l.loan_status != 'PAID_OFF'
""", nativeQuery = true)
    BigDecimal getMonthlyObligations(@Param("userId") UUID userId);

    @Query(value = """
    SELECT COALESCE(SUM(l.monthly_payment), 0)
    FROM loans l
    WHERE l.user_id = :loanId
      AND l.loan_status != 'PAID_OFF'
""", nativeQuery = true)
    BigDecimal getMonthlyObligationsByLoanId(@Param("loanId") UUID loanId);
}
