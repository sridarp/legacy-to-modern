package com.firefly.ledger.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Transaction group — groups split transactions (Stage 1: Split Transaction Support).
 * Maps to legacy 'transaction_groups' table.
 */
@Entity
@Table(name = "transaction_groups")
public class TransactionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_group_id")
    private Long userGroupId;

    @Column(length = 255)
    private String title;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public TransactionGroup() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserGroupId() { return userGroupId; }
    public void setUserGroupId(Long userGroupId) { this.userGroupId = userGroupId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}