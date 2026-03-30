package com.firefly.ledger.repository;

import com.firefly.ledger.entity.TransactionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionGroupRepository extends JpaRepository<TransactionGroup, Long> {
}