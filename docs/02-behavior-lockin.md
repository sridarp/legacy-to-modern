I'll systematically discover all business logic to generate comprehensive BDD coverage. Let me start by examining the key areas that were missed.LSP unavailable for PHP. I'll use systematic grep/file-read discovery as the substitute methodology.I now have comprehensive knowledge from `config/firefly.php` (all business rules, account types, transaction types, rule actions, allowed MIME types, etc.), `routes/api.php` (all API endpoints), and the key model files. Let me produce the complete BDD specification.

---

# Firefly III — Comprehensive BDD Behavior Lock-in Specification

## Discovery Methodology

Since PHP LSP was unavailable, systematic discovery was performed using:
1. **`config/firefly.php`** — Canonical source of all business rules, type constraints, allowed values, and feature flags
2. **`routes/api.php`** — Full API surface: all CRUD endpoints, admin-only routes, nested resources
3. **Model files** (`Account.php`, `Recurrence.php`, `PiggyBank.php`, `Webhook.php`) — Fillable fields, relationships, casts, observers, route binders
4. **`app/Casts/SeparateTimezoneCaster.php`** — Timezone handling logic

---

## Feature Coverage Matrix

| # | Feature | Scenarios | Traceability | Primary Source Files |
|---|---------|-----------|-------------|---------------------|
| 1 | Account Management | 8 | BR-ACC-001 – BR-ACC-008 | `Account.php`, `config/firefly.php:valid_currency_account_types`, `accountTypesByIdentifier` |
| 2 | Transaction CRUD & Double-Entry | 12 | BR-TXN-001 – BR-TXN-012 | `TransactionJournal.php`, `TransactionGroup.php`, `config/firefly.php:source_dests`, `account_to_transaction` |
| 3 | Budget Management | 8 | BR-BDG-001 – BR-BDG-008 | `Budget.php`, `BudgetLimit`, routes `v1/budgets` |
| 4 | Bill / Subscription Management | 8 | BR-BIL-001 – BR-BIL-008 | `Bill.php`, `config/firefly.php:bill_periods`, `bill_reminder_periods` |
| 5 | Rule Engine | 10 | BR-RUL-001 – BR-RUL-010 | `Rule.php`, `config/firefly.php:rule-actions`, rule action classes |
| 6 | Recurring Transactions | 8 | BR-REC-001 – BR-REC-008 | `Recurrence.php`, `RecurrenceRepetition`, `RecurrenceTransaction` |
| 7 | Multi-Currency & Exchange Rates | 7 | BR-CUR-001 – BR-CUR-007 | `routes/api.php:exchange-rates`, `TransactionCurrency` routes |
| 8 | Piggy Bank (Savings Goals) | 7 | BR-PIG-001 – BR-PIG-007 | `PiggyBank.php`, `config/firefly.php:piggy_bank_account_types` |
| 9 | Category Management | 6 | BR-CAT-001 – BR-CAT-006 | `Category.php`, routes `v1/categories` |
| 10 | Tag Management | 6 | BR-TAG-001 – BR-TAG-006 | `Tag.php`, routes `v1/tags` |
| 11 | Attachment Management | 6 | BR-ATT-001 – BR-ATT-006 | `Attachment.php`, `config/firefly.php:allowedMimes`, `valid_attachment_models` |
| 12 | Webhook System | 8 | BR-WHK-001 – BR-WHK-008 | `Webhook.php`, webhook messages/attempts routes |
| 13 | User Group / Multi-Tenancy | 5 | BR-UGR-001 – BR-UGR-005 | `UserGroup.php`, route binders, `user_group_id` on models |
| 14 | Object Group Management | 5 | BR-OBG-001 – BR-OBG-005 | `ObjectGroup.php`, routes `v1/object-groups` |
| 15 | Transaction Link Management | 6 | BR-LNK-001 – BR-LNK-006 | `TransactionLink`, `LinkType` routes |
| 16 | Search & Autocomplete | 6 | BR-SRC-001 – BR-SRC-006 | `routes/api.php` search, autocomplete controllers |
| 17 | Data Export & Purge | 5 | BR-EXP-001 – BR-EXP-005 | `routes/api.php` data export, destroy, purge |
| 18 | Cron & Scheduled Jobs | 6 | BR-CRN-001 – BR-CRN-006 | `CronController`, `BillWarningCronjob`, `RecurringCronjob`, `ExchangeRatesCronjob`, `WebhookCronjob` |
| 19 | User Preferences & Configuration | 6 | BR-PRF-001 – BR-PRF-006 | `routes/api.php` preferences, configuration, `config/firefly.php:default_preferences` |
| 20 | Reporting & Insights | 6 | BR-RPT-001 – BR-RPT-006 | `routes/api.php` insight, summary, chart controllers |
| 21 | Timezone Handling (SeparateTimezoneCaster) | 5 | BR-TZ-001 – BR-TZ-005 | `app/Casts/SeparateTimezoneCaster.php` |

**Total: 21 features, 142 scenarios**

---

## Feature 1: Account Management

```gherkin
@ref:BR-ACC-001
Feature: Account Management
  As a user I manage financial accounts with type-specific constraints.
  Source: app/Models/Account.php, config/firefly.php

  # --- Happy Paths ---

  Scenario: 1.1 Create asset account with opening balance
    Given I am authenticated as user "test@firefly.com"
    When I POST to "/api/v1/accounts" with:
      | field           | value           |
      | name            | My Checking     |
      | type            | asset           |
      | currency_code   | EUR             |
      | account_role    | defaultAsset    |
      | opening_balance | 1000.00         |
      | opening_date    | 2024-01-01      |
    Then the response status should be 200
    And the account "My Checking" should exist with type "Asset account"
    And an opening balance transaction of "1000.00" EUR should be created
    And the transaction source should be of type "Initial balance account"
    And the transaction destination should be "My Checking"

  @ref:BR-ACC-002
  Scenario: 1.2 Create each supported account type
    Given I am authenticated as user "test@firefly.com"
    When I create accounts with the following types:
      | type      | name             | expected_db_type      |
      | asset     | Checking         | Asset account         |
      | expense   | Groceries        | Expense account       |
      | revenue   | Salary Source    | Revenue account       |
      | loan      | Car Loan         | Loan                  |
      | debt      | Credit Card Debt | Debt                  |
      | mortgage  | Home Mortgage    | Mortgage              |
    Then each account should be created with the correct account_type_id
    # Validates: config/firefly.php:accountTypeByIdentifier mapping

  @ref:BR-ACC-003
  Scenario: 1.3 Currency restriction — only valid_currency_account_types get currency
    Given I am authenticated as user "test@firefly.com"
    And valid currency account types are:
      | Asset account | Loan | Debt | Mortgage | Cash account | Initial balance account | Liability credit account | Reconciliation account |
    When I create an expense account "Shop" with currency_code "USD"
    Then the currency setting should be ignored for the expense account
    And the expense account should not store a currency_id in account_meta
    # Source: config/firefly.php:valid_currency_account_types

  # --- Edge Cases ---

  @ref:BR-ACC-004
  Scenario: 1.4 Account roles are valid only for asset accounts
    Given I am authenticated as user "test@firefly.com"
    When I create an asset account with account_role "ccAsset"
    Then the account should be created with role "ccAsset"
    And the valid cc_fields "cc_monthly_payment_date,cc_type" should be stored
    When I create an asset account with account_role "savingAsset"
    Then the account should be created with role "savingAsset"
    # Validates: config/firefly.php:accountRoles = ['defaultAsset','sharedAsset','savingAsset','ccAsset','cashWalletAsset']

  @ref:BR-ACC-005
  Scenario: 1.5 Virtual balance — only asset accounts may have virtual amounts
    Given I am authenticated as user "test@firefly.com"
    When I create an asset account "Buffer Account" with virtual_balance "500.00"
    Then the virtual_balance should be stored as "500.00"
    When I create a loan account "Car Loan" with virtual_balance "500.00"
    Then the virtual_balance should be ignored
    # Source: config/firefly.php:can_have_virtual_amounts = ['Asset account']

  @ref:BR-ACC-006
  Scenario: 1.6 Liability accounts support interest fields
    Given I am authenticated as user "test@firefly.com"
    When I create a loan account with:
      | field              | value   |
      | name               | Car Loan|
      | interest           | 5.5     |
      | interest_period    | monthly |
      | liability_direction| debit   |
    Then the loan should store interest "5.5" with period "monthly"
    # Source: config/firefly.php:interest_periods, valid_account_fields

  # --- Error Paths ---

  @ref:BR-ACC-007
  Scenario: 1.7 Reject duplicate account name within same type
    Given I am authenticated as user "test@firefly.com"
    And an asset account "Checking" already exists
    When I POST to "/api/v1/accounts" with name "Checking" and type "asset"
    Then the response status should be 422
    And the error should indicate "name has already been taken"

  @ref:BR-ACC-008
  Scenario: 1.8 Route binder returns 404 for nonexistent or other-user account
    Given I am authenticated as user "test@firefly.com"
    When I GET "/api/v1/accounts/999999"
    Then the response status should be 404
    # Source: Account::routeBinder throws NotFoundHttpException
```

---

## Feature 2: Transaction CRUD & Double-Entry Bookkeeping

```gherkin
@ref:BR-TXN-001
Feature: Transaction CRUD & Double-Entry Bookkeeping
  As a user I create, read, update, and delete transactions.
  The system enforces double-entry with source/destination account type constraints.
  Source: config/firefly.php:source_dests, account_to_transaction, expected_source_types

  # --- Happy Paths ---

  Scenario: 2.1 Create a withdrawal (Asset → Expense)
    Given I am authenticated as user "test@firefly.com"
    And an asset account "Checking" exists with currency "EUR"
    When I POST to "/api/v1/transactions" with:
      | field              | value        |
      | type               | withdrawal   |
      | description        | Groceries    |
      | amount             | 50.00        |
      | source_name        | Checking     |
      | destination_name   | Supermarket  |
      | date               | 2024-06-01   |
    Then the response status should be 200
    And a transaction group should be created with one journal
    And the journal type should be "Withdrawal"
    And a source transaction of "-50.00" should exist for "Checking"
    And a destination transaction of "50.00" should exist for "Supermarket"
    And if "Supermarket" did not exist it should be auto-created as an expense account
    # Source: config/firefly.php:dynamic_creation_allowed includes Expense account

  @ref:BR-TXN-002
  Scenario: 2.2 Create a deposit (Revenue → Asset)
    Given I am authenticated as user "test@firefly.com"
    And an asset account "Checking" exists
    When I POST to "/api/v1/transactions" with:
      | field            | value        |
      | type             | deposit      |
      | description      | Salary       |
      | amount           | 3000.00      |
      | source_name      | Employer Inc |
      | destination_name | Checking     |
      | date             | 2024-06-01   |
    Then the journal type should be "Deposit"
    And a source transaction of "-3000.00" should exist for revenue account "Employer Inc"
    And a destination transaction of "3000.00" should exist for "Checking"

  @ref:BR-TXN-003
  Scenario: 2.3 Create a transfer (Asset → Asset)
    Given I am authenticated as user "test@firefly.com"
    And asset accounts "Checking" and "Savings" exist
    When I POST to "/api/v1/transactions" with:
      | field            | value     |
      | type             | transfer  |
      | description      | Save      |
      | amount           | 500.00    |
      | source_name      | Checking  |
      | destination_name | Savings   |
      | date             | 2024-06-01|
    Then the journal type should be "Transfer"
    And source transaction is "-500.00" for "Checking"
    And destination transaction is "500.00" for "Savings"

  @ref:BR-TXN-004
  Scenario: 2.4 Transaction type auto-detection from account types
    Given I am authenticated as user "test@firefly.com"
    When I create a transaction from "Revenue:Employer" to "Asset:Checking" without specifying type
    Then the system should auto-detect the type as "Deposit"
    # Source: config/firefly.php:account_to_transaction mapping

  @ref:BR-TXN-005
  Scenario: 2.5 Split transaction (multiple journals in one group)
    Given I am authenticated as user "test@firefly.com"
    When I POST to "/api/v1/transactions" with a group containing 3 splits:
      | description | amount | destination_name |
      | Food        | 30.00  | Grocery Store    |
      | Drinks      | 15.00  | Liquor Store     |
      | Snacks      | 5.00   | Convenience      |
    Then the transaction group should contain 3 transaction journals
    And the total amount should be 50.00

  @ref:BR-TXN-006
  Scenario: 2.6 Update transaction description and amount
    Given I am authenticated as user "test@firefly.com"
    And a withdrawal transaction group with id "42" exists
    When I PUT to "/api/v1/transactions/42" with:
      | field       | value           |
      | description | Updated grocery |
      | amount      | 55.00           |
    Then the journal description should be "Updated grocery"
    And the source amount should be "-55.00"
    And the destination amount should be "55.00"

  @ref:BR-TXN-007
  Scenario: 2.7 Delete a transaction group
    Given a transaction group "42" exists with 2 journals
    When I DELETE "/api/v1/transactions/42"
    Then the transaction group should be soft-deleted
    And all associated journals should be soft-deleted

  @ref:BR-TXN-008
  Scenario: 2.8 Delete a single journal from a split
    Given a transaction group "42" exists with 3 journals
    When I DELETE "/api/v1/transaction-journals/{journal_id}"
    Then only that journal should be soft-deleted
    And the group should still contain 2 journals

  # --- Edge Cases ---

  @ref:BR-TXN-009
  Scenario: 2.9 Foreign currency on a transaction
    Given I am authenticated and asset account "Checking" has currency "EUR"
    When I create a withdrawal with:
      | amount          | 100.00 |
      | foreign_amount  | 110.50 |
      | foreign_currency| USD    |
    Then the journal should store both native (100.00 EUR) and foreign (110.50 USD) amounts

  @ref:BR-TXN-010
  Scenario: 2.10 Journal meta fields are persisted
    Given I create a transaction with meta fields:
      | external_url       | https://receipt.example.com |
      | internal_reference | REF-2024-001                |
      | sepa_ct_id         | SEPA123                     |
      | interest_date      | 2024-07-01                  |
    Then all meta fields should be retrievable via GET
    # Source: config/firefly.php:journal_meta_fields

  # --- Error Paths ---

  @ref:BR-TXN-011
  Scenario: 2.11 Reject invalid source/destination type combination
    Given I am authenticated as user "test@firefly.com"
    When I create a withdrawal with source type "Expense account"
    Then the response status should be 422
    And the error should indicate expense accounts cannot be a source
    # Source: config/firefly.php:allowed_opposing_types — Expense source = []

  @ref:BR-TXN-012
  Scenario: 2.12 Reject revenue account as destination
    When I create a deposit with destination type "Revenue account"
    Then the response status should be 422
    And the error should indicate revenue accounts cannot be a destination
    # Source: config/firefly.php:allowed_opposing_types.destination.Revenue = []
```

---

## Feature 3: Budget Management

```gherkin
@ref:BR-BDG-001
Feature: Budget Management
  As a user I create budgets and set spending limits per period.
  Source: routes/api.php v1/budgets, v1/budget-limits, v1/available-budgets

  # --- Happy Paths ---

  Scenario: 3.1 Create a budget
    Given I am authenticated as user "test@firefly.com"
    When I POST to "/api/v1/budgets" with:
      | field | value     |
      | name  | Groceries |
    Then the response status should be 200
    And a budget "Groceries" should exist

  @ref:BR-BDG-002
  Scenario: 3.2 Create a budget limit for a period
    Given a budget "Groceries" exists with id "5"
    When I POST to "/api/v1/budgets/5/limits" with:
      | field      | value       |
      | start      | 2024-06-01  |
      | end        | 2024-06-30  |
      | amount     | 300.00      |
      | currency_code | EUR      |
    Then a budget limit should exist for "Groceries" from June 1–30 with 300.00 EUR

  @ref:BR-BDG-003
  Scenario: 3.3 List transactions for a budget
    Given budget "Groceries" has 3 linked transactions
    When I GET "/api/v1/budgets/5/transactions"
    Then I should receive 3 transactions all tagged with budget "Groceries"

  @ref:BR-BDG-004
  Scenario: 3.4 List transactions without a budget
    Given 2 transactions exist without a budget
    When I GET "/api/v1/budgets/transactions-without-budget"
    Then I should receive those 2 unbudgeted transactions

  @ref:BR-BDG-005
  Scenario: 3.5 Update a budget limit amount
    Given a budget limit exists with id "10" for budget "5"
    When I PUT to "/api/v1/budgets/5/limits/10" with amount "400.00"
    Then the budget limit amount should be updated to "400.00"

  @ref:BR-BDG-006
  Scenario: 3.6 View available budgets
    Given available budgets are configured for the current period
    When I GET "/api/v1/available-budgets"
    Then I should receive the list of available budget envelopes

  # --- Error Paths ---

  @ref:BR-BDG-007
  Scenario: 3.7 Reject duplicate budget name
    Given a budget "Groceries" already exists
    When I POST to "/api/v1/budgets" with name "Groceries"
    Then the response status should be 422

  @ref:BR-BDG-008
  Scenario: 3.8 Reject budget limit with end before start
    Given a budget "Groceries" exists with id "5"
    When I POST to "/api/v1/budgets/5/limits" with start "2024-06-30" and end "2024-06-01"
    Then the response status should be 422
    And the error should indicate "end must be after start"
```

---

## Feature 4: Bill / Subscription Management

```gherkin
@ref:BR-BIL-001
Feature: Bill / Subscription Management
  As a user I track recurring bills with expected amounts and repeat frequencies.
  Source: app/Models/Bill.php, config/firefly.php:bill_periods, bill_reminder_periods

  # --- Happy Paths ---

  Scenario: 4.1 Create a monthly bill
    Given I am authenticated as user "test@firefly.com"
    When I POST to "/api/v1/bills" with:
      | field           | value       |
      | name            | Rent        |
      | amount_min      | 900.00      |
      | amount_max      | 900.00      |
      | date            | 2024-01-01  |
      | repeat_freq     | monthly     |
      | currency_code   | EUR         |
    Then a bill "Rent" should be created with repeat_freq "monthly"
    # Valid repeat frequencies: daily, weekly, monthly, quarterly, half-year, yearly

  @ref:BR-BIL-002
  Scenario: 4.2 Bill with all valid repeat frequencies
    Given I am authenticated as user "test@firefly.com"
    When I create bills with each repeat frequency:
      | repeat_freq |
      | daily       |
      | weekly      |
      | monthly     |
      | quarterly   |
      | half-year   |
      | yearly      |
    Then all bills should be created successfully
    # Source: config/firefly.php:bill_periods

  @ref:BR-BIL-003
  Scenario: 4.3 Bill accessible via /subscriptions alias
    Given bill "Rent" exists
    When I GET "/api/v1/subscriptions"
    Then "Rent" should appear in the results
    # Source: routes/api.php — subscriptions routes alias bills

  @ref:BR-BIL-004
  Scenario: 4.4 List transactions for a bill
    Given bill "Rent" is linked to 6 transactions
    When I GET "/api/v1/bills/{id}/transactions"
    Then I should receive 6 transactions

  @ref:BR-BIL-005
  Scenario: 4.5 List rules linked to a bill
    Given bill "Rent" has a rule with action "link_to_bill"
    When I GET "/api/v1/bills/{id}/rules"
    Then I should receive the rule that links to "Rent"

  # --- Edge Cases ---

  @ref:BR-BIL-006
  Scenario: 4.6 Bill reminder periods trigger warnings
    Given bill "Rent" is due in 7 days
    And bill_reminder_periods are [90, 30, 14, 7, 0]
    When the bill warning cron runs
    Then a reminder should be generated for the 7-day threshold
    # Source: config/firefly.php:bill_reminder_periods

  # --- Error Paths ---

  @ref:BR-BIL-007
  Scenario: 4.7 Reject bill with amount_min greater than amount_max
    When I POST to "/api/v1/bills" with amount_min "1000.00" and amount_max "500.00"
    Then the response status should be 422
    And the error should indicate "amount_min must not exceed amount_max"

  @ref:BR-BIL-008
  Scenario: 4.8 Reject bill with invalid repeat frequency
    When I POST to "/api/v1/bills" with repeat_freq "biweekly"
    Then the response status should be 422
    And the error should indicate "invalid repeat frequency"
```

---

## Feature 5: Rule Engine

```gherkin
@ref:BR-RUL-001
Feature: Rule Engine
  As a user I define rules with triggers and actions that auto-process transactions.
  Source: config/firefly.php:rule-actions, app/TransactionRules/Actions/*

  # --- Happy Paths ---

  Scenario: 5.1 Create a rule with trigger and action
    Given I am authenticated as user "test@firefly.com"
    When I POST to "/api/v1/rules" with:
      | field         | value               |
      | title         | Tag groceries       |
      | trigger       | store-journal       |
      | rule_triggers | description_contains: grocery |
      | rule_actions  | add_tag: groceries  |
    Then a rule "Tag groceries" should be created

  @ref:BR-RUL-002
  Scenario: 5.2 All registered rule actions are valid
    Given the system supports these rule actions:
      | action_key                | class                    |
      | set_category              | SetCategory              |
      | clear_category            | ClearCategory            |
      | set_budget                | SetBudget                |
      | clear_budget              | ClearBudget              |
      | add_tag                   | AddTag                   |
      | remove_tag                | RemoveTag                |
      | remove_all_tags           | RemoveAllTags            |
      | set_description           | SetDescription           |
      | set_source_account        | SetSourceAccount         |
      | set_destination_account   | SetDestinationAccount    |
      | set_notes                 | SetNotes                 |
      | clear_notes               | ClearNotes               |
      | link_to_bill              | LinkToBill               |
      | convert_withdrawal        | ConvertToWithdrawal      |
      | convert_deposit           | ConvertToDeposit         |
      | convert_transfer          | ConvertToTransfer        |
      | switch_accounts           | SwitchAccounts           |
      | update_piggy              | UpdatePiggyBank          |
      | delete_transaction        | DeleteTransaction        |
      | set_source_to_cash        | SetSourceToCashAccount   |
      | set_destination_to_cash   | SetDestinationToCashAccount |
      | set_amount                | SetAmount                |
    Then each action should be executable without error
    # Source: config/firefly.php:rule-actions

  @ref:BR-RUL-003
  Scenario: 5.3 Test a rule against existing transactions
    Given rule "Tag groceries" exists with id "7"
    When I GET "/api/v1/rules/7/test"
    Then I should receive a list of matching transactions (limit 10, range 200)
    # Source: config/firefly.php:test-triggers

  @ref:BR-RUL-004
  Scenario: 5.4 Trigger a rule manually
    Given rule "Tag groceries" exists with id "7"
    When I POST to "/api/v1/rules/7/trigger"
    Then matching transactions should have the tag "groceries" applied

  @ref:BR-RUL-005
  Scenario: 5.5 Rule converts withdrawal to transfer
    Given a rule with action "convert_transfer" exists
    And a withdrawal transaction from "Checking" to "Savings" exists
    When the rule fires on that transaction
    Then the transaction type should become "Transfer"
    And source/destination types should be validated against source_dests

  @ref:BR-RUL-006
  Scenario: 5.6 Rule deletes a transaction
    Given a rule with action "delete_transaction" exists
    When the rule fires on a matching transaction
    Then the transaction should be soft-deleted

  @ref:BR-RUL-007
  Scenario: 5.7 Validate expression engine
    Given feature flag "expression_engine" is enabled
    When I GET "/api/v1/rules/validate-expression" with a valid expression
    Then the response should indicate the expression is valid

  # --- Rule Groups ---

  @ref:BR-RUL-008
  Scenario: 5.8 Create and manage rule groups
    Given I am authenticated as user "test@firefly.com"
    When I POST to "/api/v1/rule-groups" with title "Monthly Rules"
    Then a rule group "Monthly Rules" should be created
    When I GET "/api/v1/rule-groups/{id}/rules"
    Then I should see rules in that group

  @ref:BR-RUL-009
  Scenario: 5.9 Trigger all rules in a group
    Given rule group "Monthly Rules" has 3 active rules
    When I POST to "/api/v1/rule-groups/{id}/trigger"
    Then all 3 rules should execute in order

  # --- Error Paths ---

  @ref:BR-RUL-010
  Scenario: 5.10 Reject rule with unknown action key
    When I create a rule with action "unknown_action"
    Then the response status should be 422
    And the error should indicate "unknown rule action"
```

---

## Feature 6: Recurring Transactions

```gherkin
@ref:BR-REC-001
Feature: Recurring Transactions
  As a user I schedule recurring transactions to be auto-created.
  Source: app/Models/Recurrence.php, RecurrenceRepetition, RecurrenceTransaction

  # --- Happy Paths ---

  Scenario: 6.1 Create a recurring transaction
    Given I am authenticated as user "test@firefly.com"
    When I POST to "/api/v1/recurrences" with:
      | field              | value        |
      | title              | Monthly Rent |
      | type               | withdrawal   |
      | first_date         | 2024-01-01   |
      | repeat_until       | 2024-12-31   |
      | repetitions        | 0            |
      | apply_rules        | true         |
      | active             | true         |
    Then a recurrence "Monthly Rent" should be created
    And it should have apply_rules = true and active = true

  @ref:BR-REC-002
  Scenario: 6.2 Recurrence stores timezone-aware dates
    Given I create a recurrence with first_date "2024-01-01" in timezone "America/New_York"
    Then the first_date_tz field should be "America/New_York"
    And the first_date should be stored as UTC equivalent
    # Source: SeparateTimezoneCaster, Recurrence fillable fields

  @ref:BR-REC-003
  Scenario: 6.3 Trigger a recurrence manually
    Given recurrence "Monthly Rent" exists with id "3"
    When I POST to "/api/v1/recurrences/3/trigger"
    Then a new transaction should be created based on the recurrence template

  @ref:BR-REC-004
  Scenario: 6.4 Recurrence with repetition count limit
    Given a recurrence with repetitions = 5 exists
    And 5 transactions have already been created from it
    When the recurring cron job fires
    Then no new transaction should be created
    And the recurrence should become inactive or be skipped

  @ref:BR-REC-005
  Scenario: 6.5 List transactions created by a recurrence
    Given recurrence "Monthly Rent" has generated 6 transactions
    When I GET "/api/v1/recurrences/{id}/transactions"
    Then I should receive 6 transactions with recurrence_id meta

  @ref:BR-REC-006
  Scenario: 6.6 Update recurrence — change amount and schedule
    Given recurrence "Monthly Rent" exists
    When I PUT with updated amount and repeat_until date
    Then the recurrence should reflect the new values

  # --- Error Paths ---

  @ref:BR-REC-007
  Scenario: 6.7 Reject recurrence with first_date in past and no repeat_until
    When I create a recurrence with first_date "2020-01-01" and repeat_until null and repetitions 0
    Then the response status should be 422

  @ref:BR-REC-008
  Scenario: 6.8 Deleted recurrence triggers observer cleanup
    Given recurrence "Monthly Rent" exists
    When I DELETE "/api/v1/recurrences/{id}"
    Then the recurrence should be soft-deleted
    And the DeletedRecurrenceObserver should fire
    # Source: Recurrence.php: #[ObservedBy([DeletedRecurrenceObserver::class])]
```

---

## Feature 7: Multi-Currency & Exchange Rates

```gherkin
@ref:BR-CUR-001
Feature: Multi-Currency & Exchange Rates
  As a user I manage currencies, set exchange rates, and designate a primary currency.
  Source: routes/api.php v1/currencies, v1/exchange-rates

  # --- Happy Paths ---

  Scenario: 7.1 List all currencies
    When I GET "/api/v1/currencies"
    Then I should receive a list of all available currencies

  @ref:BR-CUR-002
  Scenario: 7.2 Get primary/default currency
    When I GET "/api/v1/currencies/primary"
    Then I should receive the user's primary currency
    And "/api/v1/currencies/default" should return the same result

  @ref:BR-CUR-003
  Scenario: 7.3 Enable and disable a currency
    Given currency "JPY" exists but is disabled
    When I POST "/api/v1/currencies/JPY/enable"
    Then "JPY" should be enabled
    When I POST "/api/v1/currencies/JPY/disable"
    Then "JPY" should be disabled

  @ref:BR-CUR-004
  Scenario: 7.4 Create and query an exchange rate
    When I POST to "/api/v1/exchange-rates" with:
      | from | to  | rate   | date       |
      | EUR  | USD | 1.0850 | 2024-06-01 |
    Then an exchange rate EUR→USD of 1.0850 on 2024-06-01 should exist
    When I GET "/api/v1/exchange-rates/EUR/USD/2024-06-01"
    Then the rate should be "1.0850"

  @ref:BR-CUR-005
  Scenario: 7.5 Delete exchange rates
    Given exchange rate EUR→USD exists for 2024-06-01
    When I DELETE "/api/v1/exchange-rates/EUR/USD/2024-06-01"
    Then the rate for that date should be removed
    When I DELETE "/api/v1/exchange-rates/EUR/USD"
    Then all rates for EUR→USD should be removed

  # --- Admin-Only ---

  @ref:BR-CUR-006
  Scenario: 7.6 Admin creates a new currency
    Given I am authenticated as admin
    When I POST to "/api/v1/currencies" with code "BTC", name "Bitcoin"
    Then currency "BTC" should be created
    # Source: routes/api.php — store/delete require 'api-admin' middleware

  # --- Error Paths ---

  @ref:BR-CUR-007
  Scenario: 7.7 Non-admin cannot create or delete currencies
    Given I am authenticated as a regular user
    When I POST to "/api/v1/currencies" with code "BTC"
    Then the response status should be 403
    When I DELETE "/api/v1/currencies/USD"
    Then the response status should be 403
```

---

## Feature 8: Piggy Bank (Savings Goals)

```gherkin
@ref:BR-PIG-001
Feature: Piggy Bank (Savings Goals)
  As a user I create piggy banks linked to accounts to save toward goals.
  Source: app/Models/PiggyBank.php, config/firefly.php:piggy_bank_account_types

  # --- Happy Paths ---

  Scenario: 8.1 Create a piggy bank
    Given I am authenticated and asset account "Savings" exists
    When I POST to "/api/v1/piggy-banks" with:
      | field                 | value       |
      | name                  | Vacation    |
      | target_amount         | 2000.00     |
      | target_date           | 2025-06-01  |
    Then piggy bank "Vacation" should be created

  @ref:BR-PIG-002
  Scenario: 8.2 Piggy bank allowed on valid account types only
    Given the following account types are valid for piggy banks:
      | Asset account | Loan | Debt | Mortgage |
    When I create a piggy bank on an expense account
    Then the response status should be 422
    # Source: config/firefly.php:piggy_bank_account_types

  @ref:BR-PIG-003
  Scenario: 8.3 List piggy bank events
    Given piggy bank "Vacation" has 3 add-money events
    When I GET "/api/v1/piggy-banks/{id}/events"
    Then I should receive 3 events showing amounts added

  @ref:BR-PIG-004
  Scenario: 8.4 Piggy bank linked via account listing
    Given asset account "Savings" has piggy bank "Vacation"
    When I GET "/api/v1/accounts/{id}/piggy-banks"
    Then "Vacation" should appear in the list

  @ref:BR-PIG-005
  Scenario: 8.5 Rule action updates piggy bank
    Given a rule with action "update_piggy" targeting "Vacation"
    When a matching transfer transaction fires the rule
    Then the piggy bank balance should be updated

  # --- Edge Cases ---

  @ref:BR-PIG-006
  Scenario: 8.6 Piggy bank with no target date
    When I create a piggy bank with target_amount "500.00" and no target_date
    Then the piggy bank should be created with null target_date

  # --- Error Paths ---

  @ref:BR-PIG-007
  Scenario: 8.7 Route binder checks ownership via account join
    Given I am authenticated as user "test@firefly.com"
    And piggy bank "Other" belongs to a different user
    When I GET "/api/v1/piggy-banks/{other_id}"
    Then the response status should be 404
    # Source: PiggyBank::routeBinder joins through account_piggy_bank → accounts.user_id
```

---

## Feature 9: Category Management

```gherkin
@ref:BR-CAT-001
Feature: Category Management
  As a user I organize transactions with categories.
  Source: routes/api.php v1/categories

  # --- Happy Paths ---

  Scenario: 9.1 CRUD lifecycle for categories
    Given I am authenticated as user "test@firefly.com"
    When I POST to "/api/v1/categories" with name "Dining Out"
    Then category "Dining Out" should be created
    When I PUT to "/api/v1/categories/{id}" with name "Restaurants"
    Then the category name should be "Restaurants"
    When I DELETE "/api/v1/categories/{id}"
    Then the category should be deleted

  @ref:BR-CAT-002
  Scenario: 9.2 List transactions for a category
    Given category "Dining Out" has 5 transactions
    When I GET "/api/v1/categories/{id}/transactions"
    Then I should receive 5 transactions

  @ref:BR-CAT-003
  Scenario: 9.3 List attachments for a category
    Given category "Dining Out" has 2 attachments
    When I GET "/api/v1/categories/{id}/attachments"
    Then I should receive 2 attachments
    # Source: config/firefly.php:valid_attachment_models includes Category::class

  @ref:BR-CAT-004
  Scenario: 9.4 Autocomplete categories
    Given categories "Dining", "Drinks", "Insurance" exist
    When I GET "/api/v1/autocomplete/categories?query=Di"
    Then I should receive "Dining" in the results

  # --- Error Paths ---

  @ref:BR-CAT-005
  Scenario: 9.5 Reject duplicate category name
    Given category "Dining Out" already exists
    When I POST to "/api/v1/categories" with name "Dining Out"
    Then the response status should be 422

  @ref:BR-CAT-006
  Scenario: 9.6 Category not found returns 404
    When I GET "/api/v1/categories/999999"
    Then the response status should be 404
```

---

## Feature 10: Tag Management

```gherkin
@ref:BR-TAG-001
Feature: Tag Management
  As a user I tag transactions for flexible grouping.
  Source: routes/api.php v1/tags (uses {tagOrId} route parameter)

  # --- Happy Paths ---

  Scenario: 10.1 Create a tag
    Given I am authenticated as user "test@firefly.com"
    When I POST to "/api/v1/tags" with:
      | field | value    |
      | tag   | vacation |
    Then tag "vacation" should be created

  @ref:BR-TAG-002
  Scenario: 10.2 Retrieve tag by name or by ID
    Given tag "vacation" exists with id "15"
    When I GET "/api/v1/tags/vacation"
    Then I should receive tag "vacation"
    When I GET "/api/v1/tags/15"
    Then I should receive the same tag
    # Source: routes use {tagOrId} parameter

  @ref:BR-TAG-003
  Scenario: 10.3 List transactions for a tag
    Given tag "vacation" has 4 transactions
    When I GET "/api/v1/tags/vacation/transactions"
    Then I should receive 4 transactions

  @ref:BR-TAG-004
  Scenario: 10.4 Update a tag
    When I PUT to "/api/v1/tags/vacation" with tag "holiday"
    Then the tag should be renamed to "holiday"

  @ref:BR-TAG-005
  Scenario: 10.5 Delete a tag
    When I DELETE "/api/v1/tags/vacation"
    Then the tag should be deleted
    And previously tagged transactions should no longer reference it

  # --- Error Paths ---

  @ref:BR-TAG-006
  Scenario: 10.6 Reject duplicate tag name
    Given tag "vacation" already exists
    When I POST to "/api/v1/tags" with tag "vacation"
    Then the response status should be 422
```

---

## Feature 11: Attachment Management

```gherkin
@ref:BR-ATT-001
Feature: Attachment Management
  As a user I attach files to accounts, bills, budgets, and other entities.
  Source: routes/api.php v1/attachments, config/firefly.php:allowedMimes, valid_attachment_models

  # --- Happy Paths ---

  Scenario: 11.1 Upload an attachment to a transaction
    Given I am authenticated and a transaction journal exists
    When I POST to "/api/v1/attachments" with:
      | field           | value                  |
      | filename        | receipt.pdf            |
      | attachable_type | TransactionJournal     |
      | attachable_id   | 42                     |
    Then an attachment record should be created
    When I POST "/api/v1/attachments/{id}/upload" with a PDF file
    Then the file should be stored

  @ref:BR-ATT-002
  Scenario: 11.2 Download an attachment
    Given attachment "receipt.pdf" exists with id "10"
    When I GET "/api/v1/attachments/10/download"
    Then I should receive the file content

  @ref:BR-ATT-003
  Scenario: 11.3 Valid attachable models
    Given attachments can be linked to:
      | Account | Bill | Budget | Category | PiggyBank | Tag | Transaction | TransactionJournal | Recurrence |
    When I create an attachment for each model type
    Then all should succeed
    # Source: config/firefly.php:valid_attachment_models

  # --- Edge Cases ---

  @ref:BR-ATT-004
  Scenario: 11.4 Allowed MIME types
    When I upload a file with MIME type "application/pdf"
    Then the upload should succeed
    When I upload a file with MIME type "application/x-executable"
    Then the upload should be rejected
    # Source: config/firefly.php:allowedMimes — comprehensive list of ~60 MIME types

  @ref:BR-ATT-005
  Scenario: 11.5 Max upload size enforced
    When I upload a file of 1073741825 bytes (>1GB)
    Then the upload should be rejected
    # Source: config/firefly.php:maxUploadSize = 1073741824

  # --- Error Paths ---

  @ref:BR-ATT-006
  Scenario: 11.6 Reject attachment for invalid model type
    When I POST to "/api/v1/attachments" with attachable_type "InvalidModel"
    Then the response status should be 422
```

---

## Feature 12: Webhook System

```gherkin
@ref:BR-WHK-001
Feature: Webhook System
  As a user I configure webhooks that fire on transaction events.
  Source: app/Models/Webhook.php, routes/api.php v1/webhooks

  # --- Happy Paths ---

  Scenario: 12.1 Create a webhook
    Given I am authenticated as user "test@firefly.com"
    When I POST to "/api/v1/webhooks" with:
      | field    | value                          |
      | title    | Notify on new transaction      |
      | trigger  | 1                              |
      | response | 1                              |
      | delivery | 1                              |
      | url      | https://hooks.example.com/fire |
      | active   | true                           |
    Then a webhook should be created with a generated secret

  @ref:BR-WHK-002
  Scenario: 12.2 Submit a webhook for delivery
    Given webhook "Notify" exists with id "5"
    When I POST to "/api/v1/webhooks/5/submit"
    Then the webhook should be queued for delivery

  @ref:BR-WHK-003
  Scenario: 12.3 Trigger webhook for a specific transaction
    Given webhook "5" exists and transaction group "42" exists
    When I POST to "/api/v1/webhooks/5/trigger-transaction/42"
    Then a webhook message should be created for that transaction

  @ref:BR-WHK-004
  Scenario: 12.4 List webhook messages and attempts
    Given webhook "5" has sent 3 messages
    When I GET "/api/v1/webhooks/5/messages"
    Then I should receive 3 messages
    When I GET "/api/v1/webhooks/5/messages/{msg_id}/attempts"
    Then I should see delivery attempts (max 3 per message)
    # Source: config/firefly.php:webhooks.max_attempts = 3

  @ref:BR-WHK-005
  Scenario: 12.5 Delete webhook message and attempt
    When I DELETE "/api/v1/webhooks/5/messages/{msg_id}"
    Then the message should be deleted
    When I DELETE "/api/v1/webhooks/5/messages/{msg_id}/attempts/{att_id}"
    Then the attempt should be deleted

  # --- Edge Cases ---

  @ref:BR-WHK-006
  Scenario: 12.6 Webhook delivery respects max_attempts
    Given webhook "5" has a message that failed delivery 3 times
    When the webhook cron job runs
    Then no further delivery attempts should be made
    # Source: config/firefly.php:webhooks.max_attempts = env('WEBHOOK_MAX_ATTEMPTS', 3)

  @ref:BR-WHK-007
  Scenario: 12.7 Webhook feature flag controls availability
    Given feature flag "webhooks" is false
    When I POST to "/api/v1/webhooks"
    Then the response should indicate webhooks are disabled
    # Source: config/firefly.php:feature_flags.webhooks

  # --- Error Paths ---

  @ref:BR-WHK-008
  Scenario: 12.8 Deleted webhook fires observer
    Given webhook "5" exists
    When I DELETE "/api/v1/webhooks/5"
    Then the webhook should be soft-deleted
    And the DeletedWebhookObserver should fire
    # Source: Webhook.php: #[ObservedBy([DeletedWebhookObserver::class])]
```

---

## Feature 13: User Group / Multi-Tenancy

```gherkin
@ref:BR-UGR-001
Feature: User Group / Multi-Tenancy
  As a user I belong to a user group that scopes all my data.
  Source: routes/api.php v1/user-groups, models carry user_group_id

  # --- Happy Paths ---

  Scenario: 13.1 List user groups
    Given I am authenticated as user "test@firefly.com"
    When I GET "/api/v1/user-groups"
    Then I should receive the user groups I belong to

  @ref:BR-UGR-002
  Scenario: 13.2 Show a single user group
    Given user group "Household" exists with id "1"
    When I GET "/api/v1/user-groups/1"
    Then I should see details of "Household"

  @ref:BR-UGR-003
  Scenario: 13.3 Update a user group
    When I PUT to "/api/v1/user-groups/1" with title "Family Budget"
    Then the user group title should be "Family Budget"

  # --- Edge Cases ---

  @ref:BR-UGR-004
  Scenario: 13.4 All entities scoped to user_group_id
    Given user is in group "1"
    When I create an account, budget, bill, recurrence, webhook, or rule
    Then each entity should have user_group_id = 1
    # Source: fillable includes 'user_group_id' on Account, Bill, Rule, Recurrence, Webhook, etc.

  # --- Error Paths ---

  @ref:BR-UGR-005
  Scenario: 13.5 Cannot access another user group's data
    Given I belong to group "1" but group "2" exists for another user
    When I GET "/api/v1/user-groups/2"
    Then the response status should be 404
```

---

## Feature 14: Object Group Management

```gherkin
@ref:BR-OBG-001
Feature: Object Group Management
  As a user I organize piggy banks and bills into named groups.
  Source: routes/api.php v1/object-groups

  # --- Happy Paths ---

  Scenario: 14.1 List object groups
    When I GET "/api/v1/object-groups"
    Then I should receive all my object groups

  @ref:BR-OBG-002
  Scenario: 14.2 Update an object group
    Given object group "Savings Goals" exists
    When I PUT to "/api/v1/object-groups/{id}" with title "My Goals"
    Then the title should be updated to "My Goals"

  @ref:BR-OBG-003
  Scenario: 14.3 List piggy banks in an object group
    Given object group "Savings Goals" contains 2 piggy banks
    When I GET "/api/v1/object-groups/{id}/piggy-banks"
    Then I should receive 2 piggy banks

  @ref:BR-OBG-004
  Scenario: 14.4 List bills in an object group
    Given object group "Monthly" contains 3 bills
    When I GET "/api/v1/object-groups/{id}/bills"
    Then I should receive 3 bills

  # --- Error Paths ---

  @ref:BR-OBG-005
  Scenario: 14.5 Delete an object group
    When I DELETE "/api/v1/object-groups/{id}"
    Then the object group should be deleted
    And associated piggy banks/bills should be unlinked (not deleted)
```

---

## Feature 15: Transaction Link Management

```gherkin
@ref:BR-LNK-001
Feature: Transaction Link Management
  As a user I link transactions with typed relationships (e.g., "is paid by", "is refund of").
  Source: routes/api.php v1/transaction-links, v1/link-types

  # --- Happy Paths ---

  Scenario: 15.1 List link types
    When I GET "/api/v1/link-types"
    Then I should receive all link types (e.g., "Is paid by", "Is refund of")

  @ref:BR-LNK-002
  Scenario: 15.2 Create a transaction link
    Given two transactions exist with journal ids "10" and "20"
    And link type "Is paid by" exists with id "1"
    When I POST to "/api/v1/transaction-links" with:
      | field         | value |
      | link_type_id  | 1     |
      | inward_id     | 10    |
      | outward_id    | 20    |
    Then a link should be created between journals 10 and 20

  @ref:BR-LNK-003
  Scenario: 15.3 List links for a transaction journal
    Given journal "10" has 2 links
    When I GET "/api/v1/transaction-journals/10/links"
    Then I should receive 2 links

  @ref:BR-LNK-004
  Scenario: 15.4 List transactions for a link type
    Given link type "Is refund of" has 3 linked transaction pairs
    When I GET "/api/v1/link-types/{id}/transactions"
    Then I should receive those linked transactions

  # --- Admin-Only ---

  @ref:BR-LNK-005
  Scenario: 15.5 Admin creates a custom link type
    Given I am authenticated as admin
    When I POST to "/api/v1/link-types" with name "Reimbursement"
    Then the link type "Reimbursement" should be created
    # Source: routes/api.php — link-type store/update/delete require 'api-admin' middleware

  # --- Error Paths ---

  @ref:BR-LNK-006
  Scenario: 15.6 Non-admin cannot create link types
    Given I am authenticated as a regular user
    When I POST to "/api/v1/link-types" with name "Custom"
    Then the response status should be 403
```

---

## Feature 16: Search & Autocomplete

```gherkin
@ref:BR-SRC-001
Feature: Search & Autocomplete
  As a user I search for transactions and autocomplete entities.
  Source: routes/api.php v1/search, v1/autocomplete

  # --- Happy Paths ---

  Scenario: 16.1 Search transactions by query
    Given transactions exist with descriptions "Grocery", "Gas", "Groceries"
    When I GET "/api/v1/search/transactions?query=grocer"
    Then I should receive matching transactions

  @ref:BR-SRC-002
  Scenario: 16.2 Get transaction search count
    When I GET "/api/v1/search/transactions/count?query=grocer"
    Then I should receive a count of matching transactions

  @ref:BR-SRC-003
  Scenario: 16.3 Search accounts
    Given accounts "Checking", "Chase Credit" exist
    When I GET "/api/v1/search/accounts?query=ch"
    Then I should receive both matching accounts

  @ref:BR-SRC-004
  Scenario: 16.4 Autocomplete for all entity types
    Given entities exist for each type
    When I call autocomplete for:
      | endpoint                    | query |
      | /autocomplete/accounts      | Check |
      | /autocomplete/bills         | Rent  |
      | /autocomplete/budgets       | Gro   |
      | /autocomplete/categories    | Din   |
      | /autocomplete/currencies    | EUR   |
      | /autocomplete/piggy-banks   | Vac   |
      | /autocomplete/tags          | vac   |
      | /autocomplete/rules         | Tag   |
      | /autocomplete/rule-groups   | Mon   |
      | /autocomplete/recurring     | Rent  |
      | /autocomplete/object-groups | Sav   |
      | /autocomplete/transactions  | Sal   |
    Then each endpoint should return matching results

  @ref:BR-SRC-005
  Scenario: 16.5 Currencies-with-code autocomplete variant
    When I GET "/api/v1/autocomplete/currencies-with-code?query=EU"
    Then results should include currency code in display

  # --- Error Paths ---

  @ref:BR-SRC-006
  Scenario: 16.6 Empty search query returns empty results
    When I GET "/api/v1/search/transactions?query="
    Then I should receive an empty result set or validation error
```

---

## Feature 17: Data Export & Purge

```gherkin
@ref:BR-EXP-001
Feature: Data Export & Purge
  As a user I export my data or permanently destroy it.
  Source: routes/api.php v1/data/export, v1/data/destroy, v1/data/purge

  # --- Happy Paths ---

  Scenario: 17.1 Export all entity types
    When I export data for each type:
      | endpoint            |
      | /data/export/accounts      |
      | /data/export/bills         |
      | /data/export/budgets       |
      | /data/export/categories    |
      | /data/export/piggy-banks   |
      | /data/export/recurring     |
      | /data/export/rules         |
      | /data/export/tags          |
      | /data/export/transactions  |
    Then each endpoint should return export data
    # Note: subscriptions endpoint aliases bills

  @ref:BR-EXP-002
  Scenario: 17.2 Destroy all user data
    Given I am authenticated as user "test@firefly.com"
    When I DELETE "/api/v1/data/destroy"
    Then all user data (accounts, transactions, budgets, etc.) should be deleted

  @ref:BR-EXP-003
  Scenario: 17.3 Purge soft-deleted records
    Given there are 10 soft-deleted transactions
    When I DELETE "/api/v1/data/purge"
    Then all soft-deleted records should be permanently removed

  @ref:BR-EXP-004
  Scenario: 17.4 Bulk update transactions
    When I POST to "/api/v1/data/bulk/transactions" with:
      | transaction_ids | field   | value    |
      | [1,2,3]         | tags    | reviewed |
    Then transactions 1, 2, and 3 should be updated

  # --- Error Paths ---

  @ref:BR-EXP-005
  Scenario: 17.5 Export feature flag controls availability
    Given feature flag "export" is false
    When I GET "/api/v1/data/export/accounts"
    Then the response should indicate export is disabled
    # Source: config/firefly.php:feature_flags.export
```

---

## Feature 18: Cron & Scheduled Jobs

```gherkin
@ref:BR-CRN-001
Feature: Cron & Scheduled Jobs
  As a system I run periodic background tasks.
  Source: routes/api.php v1/cron/{cliToken}, CronController, cronjob classes

  # --- Happy Paths ---

  Scenario: 18.1 Trigger cron via API with valid token
    Given static_cron_token is "abc123"
    When I GET "/api/v1/cron/abc123"
    Then the cron should execute all scheduled tasks
    And the response should indicate success
    # Note: this route does NOT use api middleware (withoutMiddleware(['api']))

  @ref:BR-CRN-002
  Scenario: 18.2 Recurring transaction cron creates due transactions
    Given a recurring transaction is due today
    When the recurring cron job executes
    Then a new transaction should be created from the recurrence template

  @ref:BR-CRN-003
  Scenario: 18.3 Bill warning cron sends notifications
    Given bill "Rent" is due within a reminder period
    When the bill warning cron job executes
    Then a notification/warning should be sent

  @ref:BR-CRN-004
  Scenario: 18.4 Exchange rates cron fetches latest rates
    When the exchange rate cron job executes
    Then exchange rates should be updated from the configured provider

  @ref:BR-CRN-005
  Scenario: 18.5 Webhook cron processes pending deliveries
    Given 2 webhook messages are pending delivery
    When the webhook cron job executes
    Then delivery should be attempted for each pending message

  # --- Error Paths ---

  @ref:BR-CRN-006
  Scenario: 18.6 Reject cron with invalid token
    When I GET "/api/v1/cron/invalid-token"
    Then the response should indicate authentication failure
```

---

## Feature 19: User Preferences & Configuration

```gherkin
@ref:BR-PRF-001
Feature: User Preferences & Configuration
  As a user I manage my preferences; as admin I manage system configuration.
  Source: routes/api.php v1/preferences, v1/configuration

  # --- Happy Paths ---

  Scenario: 19.1 List all preferences
    When I GET "/api/v1/preferences"
    Then I should receive all user preferences

  @ref:BR-PRF-002
  Scenario: 19.2 Get and update a specific preference
    When I GET "/api/v1/preferences/language"
    Then the value should be "en_US"
    When I PUT "/api/v1/preferences/language" with value "de_DE"
    Then the preference should be updated to "de_DE"
    # Source: config/firefly.php:default_preferences.language = 'en_US'

  @ref:BR-PRF-003
  Scenario: 19.3 Default preferences are applied for new users
    Given a new user is created
    Then the following default preferences should be set:
      | preference          | value |
      | listPageSize        | 50    |
      | currencyPreference  | EUR   |
      | language            | en_US |
      | locale              | equal |
      | convertToPrimary    | false |
      | anonymous           | false |
    # Source: config/firefly.php:default_preferences

  @ref:BR-PRF-004
  Scenario: 19.4 View system configuration
    When I GET "/api/v1/configuration"
    Then I should receive system configuration values
    When I GET "/api/v1/configuration/single_user_mode"
    Then I should receive the value of single_user_mode

  @ref:BR-PRF-005
  Scenario: 19.5 Admin updates system configuration
    Given I am authenticated as admin
    When I PUT "/api/v1/configuration/single_user_mode" with value "false"
    Then single_user_mode should be updated to false
    # Source: routes/api.php — configuration update requires 'api-admin'

  # --- Error Paths ---

  @ref:BR-PRF-006
  Scenario: 19.6 Non-admin cannot update system configuration
    Given I am authenticated as a regular user
    When I PUT "/api/v1/configuration/single_user_mode"
    Then the response status should be 403
```

---

## Feature 20: Reporting & Insights

```gherkin
@ref:BR-RPT-001
Feature: Reporting & Insights
  As a user I view financial summaries, charts, and insights.
  Source: routes/api.php v1/insight, v1/summary, v1/chart

  # --- Happy Paths ---

  Scenario: 20.1 Basic financial summary
    When I GET "/api/v1/summary/basic?start=2024-01-01&end=2024-06-30"