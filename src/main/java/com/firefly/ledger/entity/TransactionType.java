package com.firefly.ledger.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Transaction type entity — maps to legacy 'transaction_types' table.
 */
@Entity
@Table(name = "transaction_types")
public class TransactionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String type;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public TransactionType() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}