package com.example.banking_project.transaction.repository;

import com.example.banking_project.transaction.model.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface IncomeRepository extends JpaRepository<Income, UUID> {

   @Query("""
    SELECT COALESCE(SUM(i.amount), 0)
    FROM Income i 
    WHERE i.transaction.user.id = :userId
    AND i.transaction.transactionStatus = 'SUCCEEDED'
    AND i.transaction.createdOn >= :startDate
    """)
   BigDecimal getIncomeForLastMonths(@Param("userId") UUID userId,
                                     @Param("startDate") LocalDate startDate);
}
