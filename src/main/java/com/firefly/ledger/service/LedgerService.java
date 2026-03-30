package com.firefly.ledger.service;

import com.firefly.accounts.entity.Account;
import com.firefly.accounts.service.AccountService;
import com.firefly.ledger.entity.*;
import com.firefly.ledger.repository.*;
import com.firefly.shared.AccountTypeEnum;
import com.firefly.shared.TransactionTypeEnum;
import com.firefly.shared.TransactionTypeMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Double-entry ledger service — CAP-TXN.
 * Implements BR-TXN-001..012 from Stage 2 BDD scenarios.
 * <p>
 * Every transaction creates a journal with TWO legs:
 * - Source account: negative amount (money leaving)
 * - Destination account: positive amount (money arriving)
 */
@Service
@Transactional
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private final TransactionJournalRepository journalRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionGroupRepository groupRepository;
    private final TransactionTypeRepository typeRepository;
    private final TransactionCurrencyRepository currencyRepository;
    private final AccountService accountService;

    public LedgerService(TransactionJournalRepository journalRepository,
                         TransactionRepository transactionRepository,
                         TransactionGroupRepository groupRepository,
                         TransactionTypeRepository typeRepository,
                         TransactionCurrencyRepository currencyRepository,
                         AccountService accountService) {
        this.journalRepository = journalRepository;
        this.transactionRepository = transactionRepository;
        this.groupRepository = groupRepository;
        this.typeRepository = typeRepository;
        this.currencyRepository = currencyRepository;
        this.accountService = accountService;
    }

    /**
     * Create a single transaction with double-entry bookkeeping.
     * BR-TXN-001: Validates source/dest account types per TransactionTypeMatrix.
     * BR-TXN-002: Creates exactly 2 transaction legs (debit + credit).
     *
     * @param userId        owner user
     * @param txnTypeName   e.g. "Withdrawal", "Deposit", "Transfer"
     * @param sourceAccountId   source account ID
     * @param destAccountId     destination account ID
     * @param amount            positive amount
     * @param currencyCode      currency code (e.g. "EUR")
     * @param description       transaction description
     * @param date              transaction date
     * @return the created TransactionJournal
     */
    public TransactionJournal createTransaction(Long userId, Long userGroupId,
                                                 String txnTypeName,
                                                 Long sourceAccountId, Long destAccountId,
                                                 BigDecimal amount, String currencyCode,
                                                 String description, Instant date) {
        // Validate amount is positive
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        // Resolve transaction type
        TransactionTypeEnum txnTypeEnum = TransactionTypeEnum.fromLabel(txnTypeName);
        TransactionType txnType = typeRepository.findByType(txnTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown transaction type: " + txnTypeName));

        // Resolve accounts
        Account sourceAccount = accountService.getAccount(sourceAccountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + sourceAccountId));
        Account destAccount = accountService.getAccount(destAccountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found: " + destAccountId));

        // Validate account type constraints (BR-TXN-001)
        AccountTypeEnum sourceType = AccountTypeEnum.fromLabel(sourceAccount.getAccountType().getType());
        AccountTypeEnum destType = AccountTypeEnum.fromLabel(destAccount.getAccountType().getType());
        TransactionTypeMatrix.validate(txnTypeEnum, sourceType, destType);

        // Resolve currency
        TransactionCurrency currency = currencyRepository.findByCode(currencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown currency: " + currencyCode));

        // Create transaction group
        TransactionGroup group = new TransactionGroup();
        group.setUserId(userId);
        group.setUserGroupId(userGroupId);
        group.setTitle(description);
        group = groupRepository.save(group);

        // Create journal
        TransactionJournal journal = new TransactionJournal();
        journal.setUserId(userId);
        journal.setUserGroupId(userGroupId);
        journal.setTransactionType(txnType);
        journal.setTransactionGroup(group);
        journal.setTransactionCurrency(currency);
        journal.setDescription(description);
        journal.setDate(date);
        journal.setCompleted(true);
        journal = journalRepository.save(journal);

        // Create source leg (negative — money leaves source account)
        Transaction sourceLeg = new Transaction();
        sourceLeg.setAccountId(sourceAccountId);
        sourceLeg.setTransactionJournal(journal);
        sourceLeg.setTransactionCurrency(currency);
        sourceLeg.setAmount(amount.negate());
        sourceLeg.setNativeAmount(amount.negate());
        sourceLeg.setIdentifier(0);
        transactionRepository.save(sourceLeg);

        // Create destination leg (positive — money arrives at destination)
        Transaction destLeg = new Transaction();
        destLeg.setAccountId(destAccountId);
        destLeg.setTransactionJournal(journal);
        destLeg.setTransactionCurrency(currency);
        destLeg.setAmount(amount);
        destLeg.setNativeAmount(amount);
        destLeg.setIdentifier(1);
        transactionRepository.save(destLeg);

        log.info("Created {} journal id={} amount={} {} from account {} to account {}",
                txnTypeName, journal.getId(), amount, currencyCode, sourceAccountId, destAccountId);

        return journal;
    }

    @Transactional(readOnly = true)
    public Page<TransactionJournal> listTransactions(Long userId, Pageable pageable) {
        return journalRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TransactionJournal> listTransactionsByType(Long userId, String type, Pageable pageable) {
        return journalRepository.findByUserIdAndType(userId, type, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<TransactionJournal> getTransaction(Long id, Long userId) {
        return journalRepository.findByIdAndUserId(id, userId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionLegs(Long journalId) {
        return transactionRepository.findByJournalId(journalId);
    }

    /**
     * BR-TXN-010: Soft-delete a transaction (all legs + journal).
     */
    public void deleteTransaction(Long journalId, Long userId) {
        TransactionJournal journal = journalRepository.findByIdAndUserId(journalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + journalId));

        Instant now = Instant.now();
        journal.setDeletedAt(now);
        journalRepository.save(journal);

        List<Transaction> legs = transactionRepository.findByJournalId(journalId);
        for (Transaction leg : legs) {
            leg.setDeletedAt(now);
            transactionRepository.save(leg);
        }
        log.info("Soft-deleted transaction journal id={}", journalId);
    }
}