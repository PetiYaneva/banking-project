package com.example.banking_project.transaction.repository;

import com.example.banking_project.transaction.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.transaction.user.id = :userId
    AND e.transaction.transactionStatus = 'SUCCEEDED'
    AND e.transaction.createdOn >= :startDate
""")
    BigDecimal getExpensesForLastMonths(@Param("userId") UUID userId,
                                        @Param("startDate") LocalDate startDate);
}
