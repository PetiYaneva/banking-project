package com.example.banking_project.cryptocurrency.repository;
import com.example.banking_project.cryptocurrency.model.CryptoOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CryptoOrderRepository extends JpaRepository<CryptoOrder, UUID> {
    List<CryptoOrder> findByAccountIdOrderByExecutedAtDesc(UUID accountId);
    List<CryptoOrder> findByUserIdOrderByExecutedAtDesc(UUID userId);
    List<CryptoOrder> findByAccount_User_IdOrderByExecutedAtDesc(UUID userId);
    List<CryptoOrder> findAllByOrderByExecutedAtDesc();
}
