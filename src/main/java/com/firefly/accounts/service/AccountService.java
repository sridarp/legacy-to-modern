package com.firefly.accounts.service;

import com.firefly.accounts.entity.Account;
import com.firefly.accounts.entity.AccountType;
import com.firefly.accounts.repository.AccountRepository;
import com.firefly.accounts.repository.AccountTypeRepository;
import com.firefly.shared.AccountTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Account management service — CAP-ACC.
 * Implements BR-ACC-001..008 from Stage 2 BDD scenarios.
 */
@Service
@Transactional
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;

    public AccountService(AccountRepository accountRepository, AccountTypeRepository accountTypeRepository) {
        this.accountRepository = accountRepository;
        this.accountTypeRepository = accountTypeRepository;
    }

    /**
     * BR-ACC-001: Create account with type validation.
     * BR-ACC-002: Support all account types.
     */
    public Account createAccount(Long userId, Long userGroupId, String typeName, String name,
                                  BigDecimal openingBalance, String iban, boolean active) {
        AccountTypeEnum typeEnum = AccountTypeEnum.fromLabel(typeName);
        AccountType accountType = accountTypeRepository.findByType(typeName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown account type: " + typeName));

        // Check for duplicate names (BR-ACC-005)
        Optional<Account> existing = accountRepository.findByUserIdAndName(userId, name);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Account with name '" + name + "' already exists");
        }

        Account account = new Account();
        account.setUserId(userId);
        account.setUserGroupId(userGroupId);
        account.setAccountType(accountType);
        account.setName(name);
        account.setActive(active);
        account.setIban(iban);

        // Only set virtual balance for asset/liability accounts
        if (typeEnum == AccountTypeEnum.ASSET || typeEnum.isLiability()) {
            account.setVirtualBalance(openingBalance != null ? openingBalance : BigDecimal.ZERO);
        }

        Account saved = accountRepository.save(account);
        log.info("Created account id={} name='{}' type='{}'", saved.getId(), saved.getName(), typeName);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Account> listAccounts(Long userId, Pageable pageable) {
        return accountRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Account> listAccountsByType(Long userId, String type) {
        return accountRepository.findByUserIdAndType(userId, type);
    }

    @Transactional(readOnly = true)
    public Optional<Account> getAccount(Long id, Long userId) {
        return accountRepository.findByIdAndUserId(id, userId);
    }

    /**
     * BR-ACC-006: Update account preserves type constraints.
     */
    public Account updateAccount(Long id, Long userId, String name, boolean active, String iban) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

        if (name != null && !name.isBlank()) {
            // Check duplicate name (but allow keeping the same name)
            accountRepository.findByUserIdAndName(userId, name)
                    .filter(a -> !a.getId().equals(id))
                    .ifPresent(a -> {
                        throw new IllegalArgumentException("Account with name '" + name + "' already exists");
                    });
            account.setName(name);
        }
        account.setActive(active);
        if (iban != null) {
            account.setIban(iban.replaceAll("\\s+", ""));
        }

        Account saved = accountRepository.save(account);
        log.info("Updated account id={}", saved.getId());
        return saved;
    }

    /**
     * BR-ACC-007: Soft-delete account.
     */
    public void deleteAccount(Long id, Long userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        account.setDeletedAt(Instant.now());
        accountRepository.save(account);
        log.info("Soft-deleted account id={}", id);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "accountTypes", key = "#typeName")
    public Optional<AccountType> findAccountType(String typeName) {
        return accountTypeRepository.findByType(typeName);
    }
}