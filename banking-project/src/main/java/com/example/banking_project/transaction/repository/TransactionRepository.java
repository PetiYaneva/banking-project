package com.example.banking_project.transaction.repository;

import com.example.banking_project.transaction.model.Transaction;
import com.example.banking_project.transaction.view.TransactionTransferView;
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
    List<TransactionTransferView> getAllIncomesByUserId(@Param(value = "userId")UUID userId);

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
    List<TransactionTransferView> getAllExpensesByUserId(@Param(value = "userId")UUID userId);

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
    List<TransactionTransferView> getTransactionsByUserAndPeriod(@Param(value = "userId")UUID userId,
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
    List<TransactionTransferView> getIncomesByUserAndPeriod(@Param(value = "userId")UUID userId,
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
    List<TransactionTransferView> getExpensesByUserAndPeriod(@Param(value = "userId")UUID userId,
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
        WHERE t.account_id = :accountId
        ORDER BY t.created_on DESC
        """, nativeQuery = true)
    List<TransactionTransferView> getTransactionsByAccount(@Param(value = "accountId")UUID accountId);


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
    List<TransactionTransferView> findByUserIdAndType(
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
    List<TransactionTransferView> getAllTransactionsByUserId(@Param(value = "userId")UUID userId);


    // ===== ADMIN: списъци по период (за всички потребители) =====

    @Query(value = """
    SELECT
        t.id, t.amount, t.transaction_status AS transactionStatus, t.transaction_type AS transactionType,
        t.description, t.failure_reason AS failureReason, t.created_on AS createdOn, t.currency,
        t.is_income AS isIncome, t.is_expense AS isExpense, t.account_id AS accountId, t.user_id AS userId
    FROM transactions t
    WHERE t.created_on BETWEEN :startDate AND :endDate
    ORDER BY t.created_on DESC
    """, nativeQuery = true)
    List<TransactionTransferView> getAllByPeriod(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    @Query(value = """
    SELECT
        t.id, t.amount, t.transaction_status AS transactionStatus, t.transaction_type AS transactionType,
        t.description, t.failure_reason AS failureReason, t.created_on AS createdOn, t.currency,
        t.is_income AS isIncome, t.is_expense AS isExpense, t.account_id AS accountId, t.user_id AS userId
    FROM transactions t
    WHERE t.created_on BETWEEN :startDate AND :endDate
      AND t.transaction_type = :type
    ORDER BY t.created_on DESC
    """, nativeQuery = true)
    List<TransactionTransferView> getAllByPeriodAndType(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate,
                                                        @Param("type") String type);

    @Query(value = """
    SELECT
        t.id, t.amount, t.transaction_status AS transactionStatus, t.transaction_type AS transactionType,
        t.description, t.failure_reason AS failureReason, t.created_on AS createdOn, t.currency,
        t.is_income AS isIncome, t.is_expense AS isExpense, t.account_id AS accountId, t.user_id AS userId
    FROM transactions t
    WHERE t.created_on BETWEEN :startDate AND :endDate
      AND t.transaction_status = :status
    ORDER BY t.created_on DESC
    """, nativeQuery = true)
    List<TransactionTransferView> getAllByPeriodAndStatus(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate,
                                                          @Param("status") String status);

    @Query(value = """
    SELECT
        t.id, t.amount, t.transaction_status AS transactionStatus, t.transaction_type AS transactionType,
        t.description, t.failure_reason AS failureReason, t.created_on AS createdOn, t.currency,
        t.is_income AS isIncome, t.is_expense AS isExpense, t.account_id AS accountId, t.user_id AS userId
    FROM transactions t
    WHERE t.created_on BETWEEN :startDate AND :endDate
      AND UPPER(t.currency) = UPPER(:currency)
    ORDER BY t.created_on DESC
    """, nativeQuery = true)
    List<TransactionTransferView> getAllByPeriodAndCurrency(@Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate,
                                                            @Param("currency") String currency);

// ===== ADMIN: по акаунт + период =====

    @Query(value = """
    SELECT
        t.id, t.amount, t.transaction_status AS transactionStatus, t.transaction_type AS transactionType,
        t.description, t.failure_reason AS failureReason, t.created_on AS createdOn, t.currency,
        t.is_income AS isIncome, t.is_expense AS isExpense, t.account_id AS accountId, t.user_id AS userId
    FROM transactions t
    WHERE t.account_id = :accountId
      AND t.created_on BETWEEN :startDate AND :endDate
    ORDER BY t.created_on DESC
    """, nativeQuery = true)
    List<TransactionTransferView> getByAccountAndPeriod(@Param("accountId") UUID accountId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

// ===== ADMIN: филтри по сума =====

    @Query(value = """
    SELECT
        t.id, t.amount, t.transaction_status AS transactionStatus, t.transaction_type AS transactionType,
        t.description, t.failure_reason AS failureReason, t.created_on AS createdOn, t.currency,
        t.is_income AS isIncome, t.is_expense AS isExpense, t.account_id AS accountId, t.user_id AS userId
    FROM transactions t
    WHERE t.created_on BETWEEN :startDate AND :endDate
      AND t.amount >= :minAmount
    ORDER BY t.amount DESC, t.created_on DESC
    """, nativeQuery = true)
    List<TransactionTransferView> getAllByPeriodAndMinAmount(@Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate,
                                                             @Param("minAmount") java.math.BigDecimal minAmount);

    @Query(value = """
    SELECT
        t.id, t.amount, t.transaction_status AS transactionStatus, t.transaction_type AS transactionType,
        t.description, t.failure_reason AS failureReason, t.created_on AS createdOn, t.currency,
        t.is_income AS isIncome, t.is_expense AS isExpense, t.account_id AS accountId, t.user_id AS userId
    FROM transactions t
    WHERE t.created_on BETWEEN :startDate AND :endDate
      AND t.amount BETWEEN :minAmount AND :maxAmount
    ORDER BY t.amount DESC, t.created_on DESC
    """, nativeQuery = true)
    List<TransactionTransferView> getAllByPeriodAndAmountBetween(@Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate,
                                                                 @Param("minAmount") java.math.BigDecimal minAmount,
                                                                 @Param("maxAmount") java.math.BigDecimal maxAmount);

// ===== ADMIN: търсене по описание (full-text lite) =====

    @Query(value = """
    SELECT
        t.id, t.amount, t.transaction_status AS transactionStatus, t.transaction_type AS transactionType,
        t.description, t.failure_reason AS failureReason, t.created_on AS createdOn, t.currency,
        t.is_income AS isIncome, t.is_expense AS isExpense, t.account_id AS accountId, t.user_id AS userId
    FROM transactions t
    WHERE t.created_on BETWEEN :startDate AND :endDate
      AND (:q IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%')))
    ORDER BY t.created_on DESC
    """, nativeQuery = true)
    List<TransactionTransferView> searchAllByDescriptionInPeriod(@Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate,
                                                                 @Param("q") String query);

// ===== ADMIN: агрегати =====

    @Query(value = """
    SELECT COALESCE(SUM(t.amount), 0)
    FROM transactions t
    WHERE t.user_id = :userId
      AND t.created_on BETWEEN :startDate AND :endDate
      AND t.is_income IS TRUE
    """, nativeQuery = true)
    java.math.BigDecimal sumIncomeByUserAndPeriod(@Param("userId") UUID userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Query(value = """
    SELECT COALESCE(SUM(t.amount), 0)
    FROM transactions t
    WHERE t.user_id = :userId
      AND t.created_on BETWEEN :startDate AND :endDate
      AND t.is_expense IS TRUE
    """, nativeQuery = true)
    java.math.BigDecimal sumExpenseByUserAndPeriod(@Param("userId") UUID userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query(value = """
    SELECT
      COALESCE(SUM(CASE WHEN t.is_income  IS TRUE THEN t.amount ELSE 0 END), 0)
      -
      COALESCE(SUM(CASE WHEN t.is_expense IS TRUE THEN t.amount ELSE 0 END), 0)
    FROM transactions t
    WHERE t.user_id = :userId
      AND t.created_on BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    java.math.BigDecimal netCashFlowByUserAndPeriod(@Param("userId") UUID userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Query(value = """
    SELECT COALESCE(SUM(t.amount), 0)
    FROM transactions t
    WHERE t.account_id = :accountId
      AND t.created_on BETWEEN :startDate AND :endDate
      AND t.is_income IS TRUE
    """, nativeQuery = true)
    java.math.BigDecimal sumIncomeByAccountAndPeriod(@Param("accountId") UUID accountId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query(value = """
    SELECT COALESCE(SUM(t.amount), 0)
    FROM transactions t
    WHERE t.account_id = :accountId
      AND t.created_on BETWEEN :startDate AND :endDate
      AND t.is_expense IS TRUE
    """, nativeQuery = true)
    java.math.BigDecimal sumExpenseByAccountAndPeriod(@Param("accountId") UUID accountId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);


}
