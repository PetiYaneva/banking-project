package com.example.banking_project.transaction.repository;

import com.example.banking_project.transaction.model.Transaction;
import com.example.banking_project.web.dto.TransactionTransferResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query(value = """
        SELECT
            t.id,
            t.amount,
            t.transaction_status AS transactionStatus,
            t.transaction_type AS transactionType,
            t.description,
            t.failure_reason AS failureReason,
            t.created_on AS createdOn,
            t.currency,
            t.is_income AS isIncome,
            t.is_expense AS isExpense,
            t.account_id AS accountId,
            t.user_id AS userId
        FROM public.transactions t
        WHERE t.user_id = :userId
        AND t.is_income IS TRUE
        """, nativeQuery = true)
    List<TransactionTransferResponse> getAllIncomesByUserId(@Param(value = "userId")UUID userId);

    @Query(value = """
        SELECT
            t.id,
            t.amount,
            t.transaction_status AS transactionStatus,
            t.transaction_type AS transactionType,
            t.description,
            t.failure_reason AS failureReason,
            t.created_on AS createdOn,
            t.currency,
            t.is_income AS isIncome,
            t.is_expense AS isExpense,
            t.account_id AS accountId,
            t.user_id AS userId
        FROM public.transactions t
        WHERE t.user_id = :userId
        AND t.is_expense IS TRUE
        """, nativeQuery = true)
    List<TransactionTransferResponse> getAllExpensesByUserId(@Param(value = "userId")UUID userId);

    @Query(value = """
        SELECT
            t.id,
            t.amount,
            t.transaction_status AS transactionStatus,
            t.transaction_type AS transactionType,
            t.description,
            t.failure_reason AS failureReason,
            t.created_on AS createdOn,
            t.currency,
            t.is_income AS isIncome,
            t.is_expense AS isExpense,
            t.account_id AS accountId,
            t.user_id AS userId
        FROM transactions t
        WHERE t.user_id = :userId
        AND t.created_on BETWEEN :startDate AND :endDate
        ORDER BY t.created_on DESC
        """, nativeQuery = true)
    List<TransactionTransferResponse> getTransactionsByUserAndPeriod(@Param(value = "userId")UUID userId,
                                                                     @Param(value = "startDate")LocalDate startDate,
                                                                     @Param(value = "endDate")LocalDate endDate);

    @Query(value = """
        SELECT
            t.id,
            t.amount,
            t.transaction_status AS transactionStatus,
            t.transaction_type AS transactionType,
            t.description,
            t.failure_reason AS failureReason,
            t.created_on AS createdOn,
            t.currency,
            t.is_income AS isIncome,
            t.is_expense AS isExpense,
            t.account_id AS accountId,
            t.user_id AS userId
        FROM transactions t
        WHERE t.user_id = :userId
        AND t.created_on BETWEEN :startDate AND :endDate
        AND t.is_income IS TRUE
        ORDER BY t.created_on DESC
        """, nativeQuery = true)
    List<TransactionTransferResponse> getIncomesByUserAndPeriod(@Param(value = "userId")UUID userId,
                                                                 @Param(value = "startDate")LocalDate startDate,
                                                                 @Param(value = "endDate")LocalDate endDate);

    @Query(value = """
        SELECT
            t.id,
            t.amount,
            t.transaction_status AS transactionStatus,
            t.transaction_type AS transactionType,
            t.description,
            t.failure_reason AS failureReason,
            t.created_on AS createdOn,
            t.currency,
            t.is_income AS isIncome,
            t.is_expense AS isExpense,
            t.account_id AS accountId,
            t.user_id AS userId
        FROM transactions t
        WHERE t.user_id = :userId
          AND t.created_on BETWEEN :startDate AND :endDate
          AND t.is_expense IS TRUE
        ORDER BY t.created_on DESC
        """, nativeQuery = true)
    List<TransactionTransferResponse> getExpensesByUserAndPeriod(@Param(value = "userId")UUID userId,
                                                                  @Param(value = "startDate")LocalDate startDate,
                                                                  @Param(value = "endDate")LocalDate endDate);

    @Query(value = """
        SELECT
            t.id,
            t.amount,
            t.transaction_status AS transactionStatus,
            t.transaction_type AS transactionType,
            t.description,
            t.failure_reason AS failureReason,
            t.created_on AS createdOn,
            t.currency,
            t.is_income AS isIncome,
            t.is_expense AS isExpense,
            t.account_id AS accountId,
            t.user_id AS userId
        FROM transactions t
        WHERE t.user_id = :accountId
        ORDER BY t.created_on DESC
        """, nativeQuery = true)
    List<TransactionTransferResponse> getTransactionsByAccount(@Param(value = "accountId")UUID userId);


    @Query(value = """
    SELECT
        t.id,
        t.amount,
        t.transaction_status AS transactionStatus,
        t.transaction_type AS transactionType,
        t.description,
        t.failure_reason AS failureReason,
        t.created_on AS createdOn,
        t.currency,
        t.is_income AS isIncome,
        t.is_expense AS isExpense,
        t.account_id AS accountId,
        t.user_id AS userId
    FROM transactions t
    WHERE t.user_id = :userId
      AND t.transaction_type = :type
    ORDER BY t.created_on DESC
    """, nativeQuery = true)
    List<TransactionTransferResponse> findByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") String type
    );

    @Query(value = """
        SELECT
            t.id,
            t.amount,
            t.transaction_status AS transactionStatus,
            t.transaction_type AS transactionType,
            t.description,
            t.failure_reason AS failureReason,
            t.created_on AS createdOn,
            t.currency,
            t.is_income AS isIncome,
            t.is_expense AS isExpense,
            t.account_id AS accountId,
            t.user_id AS userId
        FROM public.transactions t
        WHERE t.user_id = :userId
        """, nativeQuery = true)
    List<TransactionTransferResponse> getAllTransactionsByUserId(@Param(value = "userId")UUID userId);

}
