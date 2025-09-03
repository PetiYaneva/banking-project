package com.example.banking_project.account.repository;

import com.example.banking_project.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findAccountById(UUID id);

    boolean existsByIban(String iban);

    Optional<Account> findAccountByIban(String iban);

    // user е релация -> a.user.id
    @Query("select a from Account a where a.user.id = :userId")
    List<Account> findAllAccountsByUserId(@Param("userId") UUID userId);

    List<Account> findAllByBalanceGreaterThanEqual(BigDecimal balance);

    List<Account> findAllByBalanceLessThanEqual(BigDecimal balance);

    // същото, но с оригиналното име
    @Query("select a from Account a where a.user.id = :userId")
    List<Account> findByUserId(@Param("userId") UUID userId);

    // името е „currency“, но полето е currencyCode
    @Query("select a from Account a where a.currencyCode = :currency")
    List<Account> findAllByCurrency(@Param("currency") String currency);

    // „active“ в модела е cryptoEnabled
    @Query("select a from Account a where a.cryptoEnabled = true")
    List<Account> findAllByActiveTrue();

    @Query("select a from Account a where a.cryptoEnabled = false")
    List<Account> findAllByActiveFalse();

    List<Account> findAllByOrderByBalanceDesc();

    // в модела няма createdOn – сортираме стабилно по id (или смени към другo поле при нужда)
    @Query("select a from Account a order by a.id asc")
    List<Account> findAllByOrderByCreatedOnAsc();
}
