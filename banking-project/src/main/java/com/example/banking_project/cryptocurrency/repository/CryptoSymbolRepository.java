package com.example.banking_project.cryptocurrency.repository;

import com.example.banking_project.cryptocurrency.model.CryptoSymbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CryptoSymbolRepository extends JpaRepository<CryptoSymbol, UUID> {
    List<CryptoSymbol> findAllByEnabledTrue();
    Optional<CryptoSymbol> findBySymbol(String symbol);
    List<CryptoSymbol> findAllBySymbolIn(Collection<String> symbols);

}
