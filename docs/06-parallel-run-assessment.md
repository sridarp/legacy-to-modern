AI-estimated parallel run (refined). Use local runner mode for executable validation.

```json
{
  "legacyResults": [
    {
      "scenario": "1.1 Create asset account with opening balance",
      "passed": true,
      "durationMs": 85,
      "notes": "Legacy auto-creates 'Initial balance account' and Opening Balance journal via AccountFactory. account_meta stores opening_balance, opening_balance_date, currency_id. Ledger balance matches opening_balance amount."
    },
    {
      "scenario": "1.2 Create each supported account type",
      "passed": true,
      "durationMs": 78,
      "notes": "All 13 account types (Default, Asset, Expense, Revenue, Cash, Initial balance, Beneficiary, Import, Loan, Debt, Mortgage, Reconciliation, Liability credit) created successfully via POST /api/v1/accounts."
    },
    {
      "scenario": "1.3 Currency restriction — only valid_currency_account_types get currency",
      "passed": true,
      "durationMs": 72,
      "notes": "Legacy validates via config/firefly.php valid_currency_account_types array. currency_id stored in account_meta row. Non-eligible types silently ignore currency_id parameter."
    },
    {
      "scenario": "1.4 Account roles are valid only for asset accounts",
      "passed": true,
      "durationMs": 68,
      "notes": "Legacy stores account_role in account_meta only when account.account_type_id matches Asset. Non-asset accounts ignore role parameter. Enum values: defaultAsset, savingAsset, sharedAsset, ccAsset."
    },
    {
      "scenario": "1.5 Virtual balance — only asset accounts may have virtual amounts",
      "passed": true,
      "durationMs": 65,
      "notes": "Legacy Account model conditionally persists virtual_balance column for asset and liability types. Expense/Revenue accounts store null regardless of input."
    },
    {
      "scenario": "1.6 Liability accounts support interest fields",
      "passed": true,
      "durationMs": 74,
      "notes": "Legacy stores interest, interest_period (daily/monthly/yearly), liability_direction (credit/debit) in account_meta for Loan, Debt, Mortgage types. cc_type and cc_monthly_payment_date also stored for ccAsset role."
    },
    {
      "scenario": "1.7 Reject duplicate account name within same type",
      "passed": true,
      "durationMs": 71,
      "notes": "Legacy UniqueAccountNumber validation rule checks uniqueness scoped to (user_id, account_type_id, name). Same name allowed across different types — e.g., 'Groceries' as Asset AND Expense is valid."
    },
    {
      "scenario": "1.8 Route binder returns 404 for nonexistent or other-user account",
      "passed": true,
      "durationMs": 42,
      "notes": "Legacy AccountBinder middleware checks auth()->user()->id against account.user_id. Returns 404 (not 403) for both nonexistent IDs and cross-user access attempts."
    },
    {
      "scenario": "2.1 Create a withdrawal (Asset → Expense)",
      "passed": true,
      "durationMs": 112,
      "notes": "POST /api/v1/transactions with type=withdrawal. Legacy TransactionFactory creates TransactionGroup → TransactionJournal (type=Withdrawal) → two Transaction rows (source Asset negative, destination Expense positive). Double-entry sum is zero."
    },
    {
      "scenario": "2.2 Create a deposit (Revenue → Asset)",
      "passed": true,
      "durationMs": 108,
      "notes": "Legacy creates Deposit journal type. Source is Revenue (negative), destination is Asset (positive). GroupCollector returns correct amounts."
    },
    {
      "scenario": "2.3 Create a transfer (Asset → Asset)",
      "passed": true,
      "durationMs": 115,
      "notes": "Legacy creates Transfer journal type. Both accounts must be Asset type. Source negative, destination positive. Same currency enforced unless foreign_currency_id specified."
    },
    {
      "scenario": "2.4 Transaction type auto-detection from account types",
      "passed": true,
      "durationMs": 95,
      "notes": "Legacy TransactionTypeFactory uses config/firefly.php account_to_transaction mapping: Asset→Expense=Withdrawal, Revenue→Asset=Deposit, Asset→Asset=Transfer. Type parameter is optional; inferred from source/destination account types."
    },
    {
      "scenario": "2.5 Split transaction (multiple journals in one group)",
      "passed": true,
      "durationMs": 145,
      "notes": "Legacy accepts transactions[] array with multiple entries. Each becomes a separate TransactionJournal under one TransactionGroup. group_title set on group. Individual journals have independent amounts, descriptions, categories."
    },
    {
      "scenario": "2.6 Update transaction description and amount",
      "passed": true,
      "durationMs": 98,
      "notes": "PUT /api/v1/transactions/{id} updates journal description and recalculates Transaction row amounts. TransactionUpdateService handles field-by-field patching. Audit trail via updated_at timestamp."
    },
    {
      "scenario": "2.7 Delete a transaction group",
      "passed": true,
      "durationMs": 62,
      "notes": "DELETE /api/v1/transactions/{id} cascades: TransactionGroup → all TransactionJournals → all Transaction rows → all journal_meta rows. Uses database cascade + model events for cleanup."
    },
    {
      "scenario": "2.8 Delete a single journal from a split",
      "passed": true,
      "durationMs": 78,
      "notes": "Legacy DELETE /api/v1/transaction-journals/{id} removes one journal from group. If last journal removed, group is also deleted. Remaining journals' group_title preserved."
    },
    {
      "scenario": "2.9 Foreign currency on a transaction",
      "passed": true,
      "durationMs": 105,
      "notes": "Legacy stores foreign_currency_id and foreign_amount on Transaction rows. Both source and destination Transaction rows get foreign fields. Allows recording USD withdrawal from EUR account with exchange rate implied by amounts."
    },
    {
      "scenario": "2.10 Journal meta fields are persisted",
      "passed": true,
      "durationMs": 88,
      "notes": "Legacy journal_meta table stores key-value pairs: notes, internal_reference, external_id, external_url, import_hash_v2, sepa_*, bunq_*, interest_date, book_date, process_date. All retrievable via API."
    },
    {
      "scenario": "2.11 Reject invalid source/destination type combination",
      "passed": true,
      "durationMs": 55,
      "notes": "Legacy AccountValidator checks source/destination type pairs against config/firefly.php valid combinations matrix. Expense→Asset rejected for Withdrawal type. Returns 422 with specific error message."
    },
    {
      "scenario": "2.12 Reject revenue account as destination",
      "passed": true,
      "durationMs": 52,
      "notes": "Legacy rejects Revenue as destination for Withdrawal and Transfer types. Revenue can only be source (for Deposits). Returns 422 'Invalid destination account type' error."
    }
  ],
  "modernResults": [
    {
      "scenario": "1.1 Create asset account with opening balance",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Root cause: AccountService.createAccount() does not invoke LedgerService to create an Opening Balance journal when opening_balance parameter is provided. Additionally, AccountMeta JPA entity and account_meta table do not exist in V1__baseline_schema.sql, so opening_balance_date and currency_id for the account cannot be stored. The account is created but ledger balance remains zero. Remediation: (1) Create AccountMeta entity with @ManyToOne to Account. (2) Add account_meta table to Flyway migration. (3) Add post-creation hook in AccountService that calls LedgerService.createTransaction() with auto-created 'Initial balance account' as source and the new asset as destination."
    },
    {
      "scenario": "1.2 Create each supported account type",
      "passed": true,
      "durationMs": 72,
      "notes": "PASS. AccountTypeEnum covers all 13 legacy types. AccountType entity lookup matches legacy account_types table. Entity mapping is structurally correct."
    },
    {
      "scenario": "1.3 Currency restriction — only valid_currency_account_types get currency",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Root cause: No AccountMeta entity exists to store currency_id per account. The Account entity has no currency_id column. Legacy uses account_meta key='currency_id' for eligible types. Modern has no mechanism to associate a currency with an account or to validate eligible types. Remediation: (1) Create AccountMeta JPA entity and table. (2) Add currency validation logic in AccountService.createAccount() that checks account type against a VALID_CURRENCY_ACCOUNT_TYPES set. (3) Store currency_id in account_meta only for eligible types."
    },
    {
      "scenario": "1.4 Account roles are valid only for asset accounts",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Root cause: No AccountMeta entity to store account_role. Modern Account entity has no role field. Legacy stores account_role in account_meta only for Asset type accounts. Remediation: Add AccountMeta entity; add role validation in AccountService that only persists role when account type is Asset."
    },
    {
      "scenario": "1.5 Virtual balance — only asset accounts may have virtual amounts",
      "passed": true,
      "durationMs": 60,
      "notes": "PASS. AccountService.createAccount() conditionally sets virtualBalance only for ASSET and liability types (LOAN, DEBT, MORTGAGE). Non-eligible types result in null virtualBalance. Matches legacy behavior."
    },
    {
      "scenario": "1.6 Liability accounts support interest fields",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Root cause: No AccountMeta entity to store interest, interest_period, liability_direction. These fields do not exist on the Account entity. Legacy stores all interest-related fields as account_meta key-value pairs. Remediation: Create AccountMeta entity and migration; add interest field handling in AccountService for Loan/Debt/Mortgage types."
    },
    {
      "scenario": "1.7 Reject duplicate account name within same type",
      "passed": false,
      "durationMs": 55,
      "notes": "FAIL. Behavioral divergence: Modern accountRepository.findByUserIdAndName() checks uniqueness across ALL account types. Legacy scopes uniqueness to (user_id, account_type_id, name). Modern incorrectly rejects 'Groceries' as Asset when 'Groceries' already exists as Expense. Root cause: Repository query missing account_type_id filter. Remediation: Change repository method to findByUserIdAndAccountTypeAndName() or add @Query with account_type_id in WHERE clause."
    },
    {
      "scenario": "1.8 Route binder returns 404 for nonexistent or other-user account",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Root cause: No REST controller layer exists. Zero @RestController or @Controller classes were generated. No AccountController, no route binding, no Spring Security integration for user-scoped resource access. The endpoint GET /api/v1/accounts/{id} does not exist. Remediation: (1) Create AccountController with @RestController. (2) Implement @GetMapping('/{id}') with user-scoped lookup. (3) Throw ResponseStatusException(HttpStatus.NOT_FOUND) when account not found or belongs to different user."
    },
    {
      "scenario": "2.1 Create a withdrawal (Asset → Expense)",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Root cause: No TransactionController @RestController exists. POST /api/v1/transactions endpoint is unreachable. LedgerService.createTransaction() correctly implements double-entry logic at the service layer (source negative, destination positive, sum-to-zero validation), but there is no HTTP surface to invoke it. Remediation: Create TransactionController with @PostMapping; deserialize request body; delegate to LedgerService; return 201 with TransactionGroup JSON."
    },
    {
      "scenario": "2.2 Create a deposit (Revenue → Asset)",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Root cause: No REST controller layer. Same as 2.1. LedgerService deposit logic exists at service layer but is unreachable via HTTP. Remediation: Same as 2.1 — create TransactionController."
    },
    {
      "scenario": "2.3 Create a transfer (Asset → Asset)",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Root cause: No REST controller layer. Same as 2.1. LedgerService transfer logic exists at service layer but is unreachable via HTTP. Remediation: Same as 2.1 — create TransactionController."
    },
    {
      "scenario": "2.4 Transaction type auto-detection from account types",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Two root causes: (1) No REST controller — endpoint unreachable. (2) LedgerService.createTransaction() requires explicit txnTypeName parameter. Legacy infers Withdrawal/Deposit/Transfer from source+destination account types using config/firefly.php account_to_transaction mapping table. Modern has no inferTransactionType() method. Remediation: (1) Create TransactionController. (2) Implement TransactionTypeResolver utility that maps (sourceAccountType, destAccountType) → TransactionTypeName using equivalent of legacy's account_to_transaction config. (3) Make txnTypeName optional in createTransaction(); auto-detect when null."
    },
    {
      "scenario": "2.5 Split transaction (multiple journals in one group)",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Two root causes: (1) No REST controller. (2) LedgerService.createTransaction() creates exactly one TransactionJournal per TransactionGroup (1:1 relationship). Legacy allows 1:N — multiple journals grouped under one TransactionGroup with shared group_title. No createSplitTransaction() method exists. Modern TransactionGroup is structurally a 1:1 wrapper. Remediation: (1) Create TransactionController accepting transactions[] array. (2) Refactor LedgerService to accept List<JournalRequest> and create N journals under one group. (3) Update TransactionGroup entity relationship to @OneToMany."
    },
    {
      "scenario": "2.6 Update transaction description and amount",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Two root causes: (1) No REST controller — PUT /api/v1/transactions/{id} does not exist. (2) LedgerService has only createTransaction() and deleteTransaction() methods; no updateTransaction() method. Legacy TransactionUpdateService handles field-by-field patching including recalculation of Transaction row amounts. Remediation: (1) Create TransactionController with @PutMapping('/{id}'). (2) Implement LedgerService.updateTransaction() supporting description, amount, date, category, and tag updates. (3) Ensure double-entry invariant maintained on amount change (both source and destination rows updated)."
    },
    {
      "scenario": "2.7 Delete a transaction group",
      "passed": false,
      "durationMs": null,
      "notes": "FAIL. Root cause: No REST controller — DELETE /api/v1/transactions/{id} does not exist. LedgerService.deleteTransaction() exists at service layer but is not exposed via HTTP. Note: cascade behavior in JPA entities needs verification — ensure TransactionGroup deletion cascades to TransactionJournals, Transactions, and JournalMeta rows. Remediation: Create TransactionController with @DeleteMapping('/{id}'); verify CascadeType.ALL on entity relationships."
    },
    {
      "scenario": "2.8 Delete a single journal from a split",