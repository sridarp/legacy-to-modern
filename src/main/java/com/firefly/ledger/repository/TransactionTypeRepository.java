package com.firefly.ledger.repository;

import com.firefly.ledger.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, Long> {
    Optional<TransactionType> findByType(String type);
}