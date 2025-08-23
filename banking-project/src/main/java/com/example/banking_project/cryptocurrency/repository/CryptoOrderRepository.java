package com.example.banking_project.cryptocurrency.repository;
import com.example.banking_project.cryptocurrency.model.CryptoOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CryptoOrderRepository extends JpaRepository<CryptoOrder, UUID> {
    List<CryptoOrder> findByAccountIdOrderByExecutedAtDesc(UUID accountId);
    List<CryptoOrder> findByUserIdOrderByExecutedAtDesc(UUID userId);
}
