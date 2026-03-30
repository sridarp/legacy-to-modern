package com.firefly.planning.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

/**
 * Budget limit — time-bound spending limits per budget.
 * Maps to legacy 'budget_limits' table.
 */
@Entity
@Table(name = "budget_limits")
public class BudgetLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @Column(name = "transaction_currency_id")
    private Long transactionCurrencyId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false, precision = 32, scale = 12)
    private BigDecimal amount;

    @Column(length = 50)
    private String period;

    @Column
    private boolean generated = false;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public BudgetLimit() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Budget getBudget() { return budget; }
    public void setBudget(Budget budget) { this.budget = budget; }
    public Long getTransactionCurrencyId() { return transactionCurrencyId; }
    public void setTransactionCurrencyId(Long id) { this.transactionCurrencyId = id; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public boolean isGenerated() { return generated; }
    public void setGenerated(boolean generated) { this.generated = generated; }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}