package com.example.banking_project.loan.repository;

import com.example.banking_project.loan.model.Loan;
import com.example.banking_project.loan.model.LoanStatus;
import com.example.banking_project.loan.view.CreditHistoryView;
import com.example.banking_project.loan.view.LoanView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    // -------- вече налични --------
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

    // ⚠️ ФИКС: тук беше user_id = :loanId. Трябва да е id = :loanId.
    @Query(value = """
        SELECT COALESCE(
            SUM(CASE WHEN l.loan_status != 'PAID_OFF' THEN l.monthly_payment ELSE 0 END), 0
        )
        FROM loans l
        WHERE l.id = :loanId
        """, nativeQuery = true)
    BigDecimal getMonthlyObligationsByLoanId(@Param("loanId") UUID loanId);

    @Query(value = """
        SELECT
          l.id                   AS id,
          l.user_id              AS userId,
          l.monthly_payment      AS monthlyPayment,
          l.remaining_amount     AS remainingAmount,
          l.next_date_of_payment AS nextDateOfPayment,
          l.loan_status          AS loanStatus,
          l.repayment_account_id AS repaymentAccountId,
          a.iban                 AS repaymentIban
        FROM loans l
        LEFT JOIN accounts a ON a.id = l.repayment_account_id
        WHERE l.loan_status = 'ACTIVE'
          AND l.next_date_of_payment <= :date
        """, nativeQuery = true)
    List<LoanView> findDueLoanViews(@Param("date") LocalDate date);

    List<Loan> getLoansByUserId(UUID userId);

    List<Loan> findAllByLoanStatus(LoanStatus status);

    List<Loan> findAllByLoanStatusAndNextDateOfPaymentBetween(
            LoanStatus status, LocalDate from, LocalDate to);

    List<Loan> findAllByDateOfApplyingBetween(LocalDate from, LocalDate to);

    List<Loan> findAllByFinalDateBetween(LocalDate from, LocalDate to);

    List<Loan> findAllByInterestRateBetween(BigDecimal minRate, BigDecimal maxRate);

    List<Loan> findAllByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    List<Loan> findAllByRemainingAmountGreaterThanEqual(BigDecimal minRemaining);

    List<Loan> findAllByRemainingAmountLessThanEqual(BigDecimal maxRemaining);

    List<Loan> findAllByMissedPaymentsGreaterThan(int minMissedPayments);

    List<Loan> findAllByLoanStatusAndNextDateOfPaymentLessThanEqual(LoanStatus status, LocalDate date);

    List<Loan> findAllByRepaymentAccount_Id(UUID accountId);

    @Query(value = """
        SELECT l.*
        FROM loans l
        JOIN accounts a ON a.id = l.repayment_account_id
        WHERE a.iban = :iban
        """, nativeQuery = true)
    List<Loan> findAllByRepaymentIban(@Param("iban") String iban);

    @Query("SELECT COALESCE(SUM(l.remainingAmount), 0) FROM Loan l WHERE l.user.id = :userId")
    BigDecimal getTotalRemainingByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user.id = :userId AND (l.loanStatus = com.example.banking_project.loan.model.LoanStatus.OVERDUE OR l.missedPayments > 0)")
    long countOverdueByUser(@Param("userId") UUID userId);

    List<Loan> findAllByOrderByNextDateOfPaymentAsc();
    List<Loan> findAllByOrderByRemainingAmountDesc();
}
