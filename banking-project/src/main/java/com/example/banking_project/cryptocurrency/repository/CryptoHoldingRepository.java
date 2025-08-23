package com.example.banking_project.cryptocurrency.repository;

import com.example.banking_project.cryptocurrency.model.CryptoHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CryptoHoldingRepository extends JpaRepository<CryptoHolding, UUID> {
    Optional<CryptoHolding> findByAccountIdAndAsset(UUID accountId, String asset);
    List<CryptoHolding> findByAccountId(UUID accountId);
    // по user
    List<CryptoHolding> findAllByAccount_User_Id(UUID userId);
}
