package com.firefly.ledger.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Transaction — individual double-entry leg. Two per journal (source + destination).
 * Source has negative amount, destination has positive amount.
 * Maps to legacy 'transactions' table.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_journal_id", nullable = false)
    private TransactionJournal transactionJournal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_currency_id")
    private TransactionCurrency transactionCurrency;

    @Column(length = 1024)
    private String description;

    @Column(nullable = false, precision = 32, scale = 12)
    private BigDecimal amount;

    @Column(name = "native_amount", precision = 32, scale = 12)
    private BigDecimal nativeAmount;

    @Column(name = "foreign_currency_id")
    private Long foreignCurrencyId;

    @Column(name = "foreign_amount", precision = 32, scale = 12)
    private BigDecimal foreignAmount;

    @Column(name = "native_foreign_amount", precision = 32, scale = 12)
    private BigDecimal nativeForeignAmount;

    @Column
    private boolean reconciled = false;

    @Column
    private int identifier = 0;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Transaction() {}

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public TransactionJournal getTransactionJournal() { return transactionJournal; }
    public void setTransactionJournal(TransactionJournal journal) { this.transactionJournal = journal; }
    public TransactionCurrency getTransactionCurrency() { return transactionCurrency; }
    public void setTransactionCurrency(TransactionCurrency currency) { this.transactionCurrency = currency; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getNativeAmount() { return nativeAmount; }
    public void setNativeAmount(BigDecimal nativeAmount) { this.nativeAmount = nativeAmount; }
    public Long getForeignCurrencyId() { return foreignCurrencyId; }
    public void setForeignCurrencyId(Long foreignCurrencyId) { this.foreignCurrencyId = foreignCurrencyId; }
    public BigDecimal getForeignAmount() { return foreignAmount; }
    public void setForeignAmount(BigDecimal foreignAmount) { this.foreignAmount = foreignAmount; }
    public BigDecimal getNativeForeignAmount() { return nativeForeignAmount; }
    public void setNativeForeignAmount(BigDecimal nativeForeignAmount) { this.nativeForeignAmount = nativeForeignAmount; }
    public boolean isReconciled() { return reconciled; }
    public void setReconciled(boolean reconciled) { this.reconciled = reconciled; }
    public int getIdentifier() { return identifier; }
    public void setIdentifier(int identifier) { this.identifier = identifier; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}