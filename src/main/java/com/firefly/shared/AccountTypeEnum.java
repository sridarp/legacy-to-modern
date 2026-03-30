package com.firefly.shared;

/**
 * Account type enumeration — mirrors legacy config/firefly.php accountTypeByIdentifier.
 * Source: app/Enums/AccountTypeEnum.php
 */
public enum AccountTypeEnum {

    ASSET("Asset account"),
    EXPENSE("Expense account"),
    REVENUE("Revenue account"),
    CASH("Cash account"),
    INITIAL_BALANCE("Initial balance account"),
    LOAN("Loan"),
    DEBT("Debt"),
    MORTGAGE("Mortgage"),
    RECONCILIATION("Reconciliation account"),
    LIABILITY_CREDIT("Liability credit account"),
    BENEFICIARY("Beneficiary account"),
    IMPORT("Import account"),
    CREDIT_CARD("Credit card");

    private final String label;

    AccountTypeEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AccountTypeEnum fromLabel(String label) {
        for (AccountTypeEnum e : values()) {
            if (e.label.equalsIgnoreCase(label)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown account type: " + label);
    }

    /** Check if this type is a liability (Loan, Debt, Mortgage). */
    public boolean isLiability() {
        return this == LOAN || this == DEBT || this == MORTGAGE;
    }

    /** Check if this type can have a currency set. */
    public boolean canHaveCurrency() {
        return this == ASSET || this == LOAN || this == DEBT || this == MORTGAGE
               || this == CASH || this == INITIAL_BALANCE || this == LIABILITY_CREDIT
               || this == RECONCILIATION;
    }
}