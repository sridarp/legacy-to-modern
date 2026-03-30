package com.firefly.shared;

import java.util.*;

/**
 * Encodes the double-entry type constraint matrix from legacy config/firefly.php:474-575.
 * Determines which source/destination account types are valid for each transaction type.
 * <p>
 * BR-TXN-001: Withdrawal must go Asset/Liability → Expense/Cash
 * BR-TXN-002: Deposit must go Revenue/Cash → Asset/Liability
 * BR-TXN-003: Transfer must go Asset/Liability → Asset/Liability
 */
public final class TransactionTypeMatrix {

    private static final Map<TransactionTypeEnum, Set<AccountTypeEnum>> VALID_SOURCES = new EnumMap<>(TransactionTypeEnum.class);
    private static final Map<TransactionTypeEnum, Set<AccountTypeEnum>> VALID_DESTINATIONS = new EnumMap<>(TransactionTypeEnum.class);

    static {
        // Sources (from config/firefly.php expected_source_types.source)
        VALID_SOURCES.put(TransactionTypeEnum.WITHDRAWAL,
                EnumSet.of(AccountTypeEnum.ASSET, AccountTypeEnum.LOAN, AccountTypeEnum.DEBT, AccountTypeEnum.MORTGAGE));
        VALID_SOURCES.put(TransactionTypeEnum.DEPOSIT,
                EnumSet.of(AccountTypeEnum.LOAN, AccountTypeEnum.DEBT, AccountTypeEnum.MORTGAGE, AccountTypeEnum.REVENUE, AccountTypeEnum.CASH));
        VALID_SOURCES.put(TransactionTypeEnum.TRANSFER,
                EnumSet.of(AccountTypeEnum.ASSET, AccountTypeEnum.LOAN, AccountTypeEnum.DEBT, AccountTypeEnum.MORTGAGE));
        VALID_SOURCES.put(TransactionTypeEnum.OPENING_BALANCE,
                EnumSet.of(AccountTypeEnum.INITIAL_BALANCE, AccountTypeEnum.ASSET, AccountTypeEnum.LOAN, AccountTypeEnum.DEBT, AccountTypeEnum.MORTGAGE));
        VALID_SOURCES.put(TransactionTypeEnum.RECONCILIATION,
                EnumSet.of(AccountTypeEnum.RECONCILIATION, AccountTypeEnum.ASSET));
        VALID_SOURCES.put(TransactionTypeEnum.LIABILITY_CREDIT,
                EnumSet.of(AccountTypeEnum.LIABILITY_CREDIT, AccountTypeEnum.LOAN, AccountTypeEnum.DEBT, AccountTypeEnum.MORTGAGE));

        // Destinations (from config/firefly.php expected_source_types.destination)
        VALID_DESTINATIONS.put(TransactionTypeEnum.WITHDRAWAL,
                EnumSet.of(AccountTypeEnum.LOAN, AccountTypeEnum.DEBT, AccountTypeEnum.MORTGAGE, AccountTypeEnum.EXPENSE, AccountTypeEnum.CASH));
        VALID_DESTINATIONS.put(TransactionTypeEnum.DEPOSIT,
                EnumSet.of(AccountTypeEnum.ASSET, AccountTypeEnum.LOAN, AccountTypeEnum.DEBT, AccountTypeEnum.MORTGAGE));
        VALID_DESTINATIONS.put(TransactionTypeEnum.TRANSFER,
                EnumSet.of(AccountTypeEnum.ASSET, AccountTypeEnum.LOAN, AccountTypeEnum.DEBT, AccountTypeEnum.MORTGAGE));
        VALID_DESTINATIONS.put(TransactionTypeEnum.OPENING_BALANCE,
                EnumSet.of(AccountTypeEnum.INITIAL_BALANCE, AccountTypeEnum.ASSET, AccountTypeEnum.LOAN, AccountTypeEnum.DEBT, AccountTypeEnum.MORTGAGE));
        VALID_DESTINATIONS.put(TransactionTypeEnum.RECONCILIATION,
                EnumSet.of(AccountTypeEnum.RECONCILIATION, AccountTypeEnum.ASSET));
        VALID_DESTINATIONS.put(TransactionTypeEnum.LIABILITY_CREDIT,
                EnumSet.of(AccountTypeEnum.LIABILITY_CREDIT, AccountTypeEnum.LOAN, AccountTypeEnum.DEBT, AccountTypeEnum.MORTGAGE));
    }

    private TransactionTypeMatrix() {
    }

    /**
     * Validate that source/dest account types are allowed for the given transaction type.
     *
     * @throws IllegalArgumentException if the combination is invalid
     */
    public static void validate(TransactionTypeEnum txnType, AccountTypeEnum sourceType, AccountTypeEnum destType) {
        Set<AccountTypeEnum> validSources = VALID_SOURCES.getOrDefault(txnType, EnumSet.noneOf(AccountTypeEnum.class));
        Set<AccountTypeEnum> validDests = VALID_DESTINATIONS.getOrDefault(txnType, EnumSet.noneOf(AccountTypeEnum.class));

        if (!validSources.contains(sourceType)) {
            throw new IllegalArgumentException(
                    String.format("Invalid source account type '%s' for transaction type '%s'. Expected one of: %s",
                            sourceType.getLabel(), txnType.getLabel(), validSources));
        }
        if (!validDests.contains(destType)) {
            throw new IllegalArgumentException(
                    String.format("Invalid destination account type '%s' for transaction type '%s'. Expected one of: %s",
                            destType.getLabel(), txnType.getLabel(), validDests));
        }
    }

    public static boolean isValidSource(TransactionTypeEnum txnType, AccountTypeEnum accountType) {
        return VALID_SOURCES.getOrDefault(txnType, EnumSet.noneOf(AccountTypeEnum.class)).contains(accountType);
    }

    public static boolean isValidDestination(TransactionTypeEnum txnType, AccountTypeEnum accountType) {
        return VALID_DESTINATIONS.getOrDefault(txnType, EnumSet.noneOf(AccountTypeEnum.class)).contains(accountType);
    }
}