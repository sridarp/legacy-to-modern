package com.firefly.accounts.repository;

import com.firefly.accounts.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.deletedAt IS NULL ORDER BY a.order, a.name")
    Page<Account> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM Account a JOIN a.accountType at WHERE a.userId = :userId AND at.type = :type AND a.deletedAt IS NULL ORDER BY a.order, a.name")
    List<Account> findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.userId = :userId AND a.deletedAt IS NULL")
    Optional<Account> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.name = :name AND a.deletedAt IS NULL")
    Optional<Account> findByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);
}