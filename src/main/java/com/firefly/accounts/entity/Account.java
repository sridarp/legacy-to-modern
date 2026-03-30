package com.firefly.accounts.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Account JPA entity — maps to legacy 'accounts' table.
 * CAP-ACC: Account Management. Stage 3 Domain D1.
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_group_id")
    private Long userGroupId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_type_id", nullable = false)
    private AccountType accountType;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "virtual_balance", precision = 32, scale = 12)
    private BigDecimal virtualBalance;

    @Column(name = "native_virtual_balance", precision = 32, scale = 12)
    private BigDecimal nativeVirtualBalance;

    @Column(length = 255)
    private String iban;

    @Column(name = "order_col")
    private int order = 0;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Account() {
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getUserGroupId() { return userGroupId; }
    public void setUserGroupId(Long userGroupId) { this.userGroupId = userGroupId; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public BigDecimal getVirtualBalance() { return virtualBalance; }
    public void setVirtualBalance(BigDecimal virtualBalance) { this.virtualBalance = virtualBalance; }

    public BigDecimal getNativeVirtualBalance() { return nativeVirtualBalance; }
    public void setNativeVirtualBalance(BigDecimal nativeVirtualBalance) { this.nativeVirtualBalance = nativeVirtualBalance; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return deletedAt != null; }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}