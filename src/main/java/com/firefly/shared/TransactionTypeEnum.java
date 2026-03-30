package com.firefly.shared;

/**
 * Transaction type enumeration — mirrors legacy TransactionTypeEnum.php.
 * Source: app/Enums/TransactionTypeEnum.php
 */
public enum TransactionTypeEnum {

    WITHDRAWAL("Withdrawal"),
    DEPOSIT("Deposit"),
    TRANSFER("Transfer"),
    OPENING_BALANCE("Opening balance"),
    RECONCILIATION("Reconciliation"),
    LIABILITY_CREDIT("Liability credit"),
    INVALID("Invalid");

    private final String label;

    TransactionTypeEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TransactionTypeEnum fromLabel(String label) {
        for (TransactionTypeEnum e : values()) {
            if (e.label.equalsIgnoreCase(label)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type: " + label);
    }
}