package com.example.banking_project.account.repository;

import com.example.banking_project.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findAccountById(UUID id);

    boolean existsByIban(String iban);

    Optional<Account> findAccountByIban(String iban);

    List<Account> findAllAccountsByUserId(UUID userId);
}
