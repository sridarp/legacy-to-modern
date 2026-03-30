package com.firefly.ledger.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Transaction journal — the double-entry header record.
 * Each journal has exactly two Transaction legs (source debit + destination credit).
 * Maps to legacy 'transaction_journals' table.
 * CAP-TXN: Stage 3 Domain D2.
 */
@Entity
@Table(name = "transaction_journals")
public class TransactionJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_group_id")
    private Long userGroupId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_type_id", nullable = false)
    private TransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_group_id")
    private TransactionGroup transactionGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_currency_id")
    private TransactionCurrency transactionCurrency;

    @Column(name = "bill_id")
    private Long billId;

    @Column(nullable = false, length = 1024)
    private String description;

    @Column(nullable = false)
    private Instant date;

    @Column(name = "date_tz", length = 100)
    private String dateTimezone = "UTC";

    @Column(name = "order_col")
    private int order = 0;

    @Column(name = "tag_count")
    private int tagCount = 0;

    @Column
    private boolean completed = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public TransactionJournal() {}

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserGroupId() { return userGroupId; }
    public void setUserGroupId(Long userGroupId) { this.userGroupId = userGroupId; }
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    public TransactionGroup getTransactionGroup() { return transactionGroup; }
    public void setTransactionGroup(TransactionGroup transactionGroup) { this.transactionGroup = transactionGroup; }
    public TransactionCurrency getTransactionCurrency() { return transactionCurrency; }
    public void setTransactionCurrency(TransactionCurrency transactionCurrency) { this.transactionCurrency = transactionCurrency; }
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }
    public String getDateTimezone() { return dateTimezone; }
    public void setDateTimezone(String dateTimezone) { this.dateTimezone = dateTimezone; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public int getTagCount() { return tagCount; }
    public void setTagCount(int tagCount) { this.tagCount = tagCount; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}