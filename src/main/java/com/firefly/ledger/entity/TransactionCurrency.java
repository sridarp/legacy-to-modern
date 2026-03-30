package com.firefly.ledger.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Transaction currency entity — maps to legacy 'transaction_currencies' table.
 */
@Entity
@Table(name = "transaction_currencies")
public class TransactionCurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 10)
    private String symbol;

    @Column(name = "decimal_places")
    private int decimalPlaces = 2;

    @Column
    private boolean enabled = true;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public TransactionCurrency() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public int getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(int decimalPlaces) { this.decimalPlaces = decimalPlaces; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}