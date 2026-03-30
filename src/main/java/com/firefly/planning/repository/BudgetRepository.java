package com.firefly.planning.repository;

import com.firefly.planning.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.deletedAt IS NULL ORDER BY b.order, b.name")
    List<Budget> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Budget b WHERE b.id = :id AND b.userId = :userId AND b.deletedAt IS NULL")
    Optional<Budget> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.name = :name AND b.deletedAt IS NULL")
    Optional<Budget> findByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);
}