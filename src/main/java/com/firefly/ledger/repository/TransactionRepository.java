package com.firefly.ledger.repository;

import com.firefly.ledger.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.transactionJournal.id = :journalId AND t.deletedAt IS NULL")
    List<Transaction> findByJournalId(@Param("journalId") Long journalId);

    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);
}