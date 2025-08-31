package com.example.banking_project.cryptocurrency.repository;

import com.example.banking_project.cryptocurrency.model.CryptoHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CryptoHoldingRepository extends JpaRepository<CryptoHolding, UUID> {

    Optional<CryptoHolding> findByAccount_IdAndAsset(UUID accountId, String asset);

    List<CryptoHolding> findAllByAccount_Id(UUID accountId);

    List<CryptoHolding> findAllByIban(String iban);

    List<CryptoHolding> findAllByUserId(UUID userId);

    Optional<CryptoHolding> findByIbanAndAsset(String iban, String asset);
}
