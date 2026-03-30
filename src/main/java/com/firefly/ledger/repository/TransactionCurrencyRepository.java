package com.firefly.ledger.repository;

import com.firefly.ledger.entity.TransactionCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionCurrencyRepository extends JpaRepository<TransactionCurrency, Long> {
    Optional<TransactionCurrency> findByCode(String code);
}