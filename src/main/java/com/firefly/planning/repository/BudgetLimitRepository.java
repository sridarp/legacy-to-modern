package com.firefly.planning.repository;

import com.firefly.planning.entity.BudgetLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {

    @Query("SELECT bl FROM BudgetLimit bl WHERE bl.budget.id = :budgetId ORDER BY bl.startDate DESC")
    List<BudgetLimit> findByBudgetId(@Param("budgetId") Long budgetId);
}