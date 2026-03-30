package com.firefly.ledger.repository;

import com.firefly.ledger.entity.TransactionJournal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TransactionJournalRepository extends JpaRepository<TransactionJournal, Long> {

    @Query("SELECT j FROM TransactionJournal j WHERE j.userId = :userId AND j.deletedAt IS NULL ORDER BY j.date DESC")
    Page<TransactionJournal> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT j FROM TransactionJournal j JOIN j.transactionType tt " +
           "WHERE j.userId = :userId AND tt.type = :type AND j.deletedAt IS NULL ORDER BY j.date DESC")
    Page<TransactionJournal> findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type, Pageable pageable);

    @Query("SELECT j FROM TransactionJournal j WHERE j.id = :id AND j.userId = :userId AND j.deletedAt IS NULL")
    Optional<TransactionJournal> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT j FROM TransactionJournal j WHERE j.userId = :userId AND j.date BETWEEN :start AND :end AND j.deletedAt IS NULL ORDER BY j.date DESC")
    Page<TransactionJournal> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                       @Param("start") Instant start,
                                                       @Param("end") Instant end,
                                                       Pageable pageable);
}