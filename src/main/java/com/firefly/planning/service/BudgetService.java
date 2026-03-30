package com.firefly.planning.service;

import com.firefly.planning.entity.Budget;
import com.firefly.planning.entity.BudgetLimit;
import com.firefly.planning.repository.BudgetLimitRepository;
import com.firefly.planning.repository.BudgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Budget service — CAP-BDG.
 * BR-BDG-001: Only withdrawals can be assigned to budgets (enforced at controller level).
 * BR-BDG-002: Budget limits have time-based amounts.
 */
@Service
@Transactional
public class BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgetRepository;
    private final BudgetLimitRepository budgetLimitRepository;

    public BudgetService(BudgetRepository budgetRepository, BudgetLimitRepository budgetLimitRepository) {
        this.budgetRepository = budgetRepository;
        this.budgetLimitRepository = budgetLimitRepository;
    }

    public Budget createBudget(Long userId, Long userGroupId, String name) {
        budgetRepository.findByUserIdAndName(userId, name).ifPresent(b -> {
            throw new IllegalArgumentException("Budget '" + name + "' already exists");
        });

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setUserGroupId(userGroupId);
        budget.setName(name);
        budget.setActive(true);

        Budget saved = budgetRepository.save(budget);
        log.info("Created budget id={} name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Budget> listBudgets(Long userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Budget> getBudget(Long id, Long userId) {
        return budgetRepository.findByIdAndUserId(id, userId);
    }

    public Budget updateBudget(Long id, Long userId, String name, boolean active) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + id));
        if (name != null && !name.isBlank()) {
            budget.setName(name);
        }
        budget.setActive(active);
        return budgetRepository.save(budget);
    }

    public void deleteBudget(Long id, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + id));
        budget.setDeletedAt(Instant.now());
        budgetRepository.save(budget);
        log.info("Soft-deleted budget id={}", id);
    }

    /**
     * BR-BDG-002: Create a budget limit (time-bound spending cap).
     */
    public BudgetLimit createBudgetLimit(Long budgetId, Long userId,
                                          BigDecimal amount, LocalDate startDate, LocalDate endDate,
                                          Long currencyId, String period) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + budgetId));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget limit amount must be positive");
        }

        BudgetLimit limit = new BudgetLimit();
        limit.setBudget(budget);
        limit.setAmount(amount);
        limit.setStartDate(startDate);
        limit.setEndDate(endDate);
        limit.setTransactionCurrencyId(currencyId);
        limit.setPeriod(period);

        BudgetLimit saved = budgetLimitRepository.save(limit);
        log.info("Created budget limit id={} for budget id={} amount={}", saved.getId(), budgetId, amount);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<BudgetLimit> getBudgetLimits(Long budgetId) {
        return budgetLimitRepository.findByBudgetId(budgetId);
    }
}