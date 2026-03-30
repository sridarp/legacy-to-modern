package com.firefly.planning.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Budget entity — CAP-BDG. Maps to legacy 'budgets' table.
 * BR-BDG-001: Only withdrawals can be budgeted.
 */
@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_group_id")
    private Long userGroupId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column
    private boolean active = true;

    @Column(name = "order_col")
    private int order = 0;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Budget() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserGroupId() { return userGroupId; }
    public void setUserGroupId(Long userGroupId) { this.userGroupId = userGroupId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}