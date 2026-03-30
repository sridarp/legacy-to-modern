package com.firefly.accounts.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Account Type entity — maps to legacy 'account_types' table.
 * Lookup table for account classifications.
 */
@Entity
@Table(name = "account_types")
public class AccountType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String type;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public AccountType() {
    }

    public AccountType(String type) {
        this.type = type;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}