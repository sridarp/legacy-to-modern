I'll analyze the Firefly III codebase to extract comprehensive migration intent documentation. Let me start by understanding the codebase structure.This is a large Laravel-based PHP application (1457 PHP files). Let me explore key structural files first.Now let me explore the core application structure and domain models:Let me try a different approach:Let me look at the database structure and config:Let me try with simpler patterns:Let me check the directory structure from file_stats:Now let me read key model and migration files to understand the domain:Now let me read a few more key files to understand the business logic:Now let me read some key business rule implementations to understand the application logic:Excellent! Now I have enough information to generate a comprehensive migration intent document. Let me compile the findings:

## Firefly III Migration Intent Document

# Business Goals

**Primary Purpose:** Firefly III is a self-hosted personal finance manager that helps users track expenses, income, budgets, bills, and savings goals without uploading financial data to the cloud. The system empowers users to gain insight and control over their finances through detailed tracking and reporting.

## Business Rules

### Account Management Rules
**Source:** `app/Models/Account.php`, `config/firefly.php` (lines 360-500)

1. **Account Type Hierarchy** - Accounts are categorized by type (Asset, Expense, Revenue, Liability) with specific rules for each:
   - **Assets** can have virtual balances and opening balances (`config/firefly.php:587-588`)
   - **Liabilities** include Loan, Debt, Mortgage, and Credit Card types
   - Only Assets can be marked to include/exclude from net worth calculations
   - Account types determine allowed transaction types (`config/firefly.php:476-525`)

2. **Account Currency Rules** - Only specific account types can have currency settings:
   - Valid types: Asset, Loan, Debt, Mortgage, Cash, Initial Balance, Liability Credit, Reconciliation (`config/firefly.php:352-361`)

3. **Account Soft Delete** - Accounts use soft deletes to preserve historical data (`app/Models/Account.php:57`)

### Transaction Rules
**Source:** `app/Models/Transaction.php`, `app/Models/TransactionJournal.php`, `config/firefly.php`

1. **Double-Entry Bookkeeping** - All transactions follow double-entry principles:
   - Each transaction has a source and destination account
   - Transaction types determine valid account combinations (`config/firefly.php:526-573`)
   - Amount must balance between source and destination

2. **Transaction Type Validation** (`config/firefly.php:447-475`):
   - **Withdrawal**: Asset/Liability → Expense/Cash
   - **Deposit**: Revenue/Cash → Asset/Liability
   - **Transfer**: Asset/Liability → Asset/Liability
   - **Opening Balance**: Initial Balance ↔ Asset/Liability
   - **Reconciliation**: Reconciliation ↔ Asset
   - **Liability Credit**: Liability Credit → Debt/Loan/Mortgage

3. **Foreign Currency Support** - Transactions can have both native and foreign amounts:
   - Native amount is always calculated in primary currency
   - Foreign amount tracks original transaction currency (`app/Models/Transaction.php:41-54`)

4. **Transaction Reconciliation** - Transactions can be marked as reconciled to prevent modification (`app/Models/Transaction.php:52`)

5. **Transaction Groups** - Multiple journals can be grouped as splits (`app/Models/TransactionJournal.php:46`)

### Budget Rules
**Source:** `app/Models/Budget.php`, `app/TransactionRules/Actions/SetBudget.php`

1. **Budget Assignment** - Only withdrawals can be assigned to budgets:
   - Rule enforced in `SetBudget.php:54-63`
   - Error message: "Cannot set budget for transaction type {type}"

2. **Budget Limits** - Budgets can have time-based limits with amounts (`config/firefly.php:267-282`)

3. **Auto-Budgets** - System can automatically create budget limits on schedule (`app/Console/Commands/Tools/Cron.php:131-147`)

### Bill (Subscription) Rules
**Source:** `app/Models/Bill.php`, `app/Console/Commands/Tools/Cron.php`

1. **Bill Matching** - Bills automatically match transactions based on:
   - Amount range (min/max) (`app/Models/Bill.php:96-97`)
   - Text matching patterns
   - Repeat frequency (daily, weekly, monthly, quarterly, half-year, yearly) (`config/firefly.php:329`)

2. **Bill Payment Tracking** - System tracks:
   - Expected payment dates
   - Actual payment dates via linked transactions
   - Skip periods for irregular payments (`app/Models/Bill.php:82`)

3. **Bill Warnings** - Cron job sends notifications for upcoming bill payments (`app/Console/Commands/Tools/Cron.php:183-200`)

### Rule Engine
**Source:** `app/Models/Rule.php`, `config/firefly.php:296-326`

1. **Rule Structure** - Rules consist of:
   - Triggers (conditions to match transactions)
   - Actions (modifications to apply)
   - Order of execution (priority)
   - Stop processing flag

2. **Available Rule Actions** (`config/firefly.php:296-326`):
   - Set/Clear category, budget, notes
   - Add/Remove tags
   - Change description
   - Convert transaction type
   - Link to bill
   - Update piggy bank
   - Delete transaction
   - Set source/destination accounts
   - Set amount

3. **Rule Execution** - Rules can be:
   - Applied automatically on transaction creation
   - Applied manually to existing transactions
   - Applied on import

4. **Strict Mode** - Rules can be set to strict matching (`app/Models/Rule.php:52`)

### Recurring Transaction Rules
**Source:** `app/Models/Recurrence.php`, `app/Console/Commands/Tools/Cron.php`

1. **Recurrence Patterns** - Support for various repetition patterns:
   - Daily, weekly, monthly, quarterly, half-yearly, yearly
   - Custom intervals with skip weekends
   - First/last date boundaries
   - Maximum repetition count (`app/Models/Recurrence.php:48-57`)

2. **Recurring Execution** - Cron job creates transactions:
   - Checks `first_date`, `repeat_until`, `latest_date` boundaries
   - Respects `active` flag
   - Can optionally apply rules to created transactions (`app/Models/Recurrence.php:57`)

### Piggy Bank (Savings Goal) Rules
**Source:** `config/firefly.php:589`

1. **Piggy Bank Account Types** - Can be attached to:
   - Asset accounts
   - Loan accounts
   - Debt accounts
   - Mortgage accounts

2. **Multi-Account Piggy Banks** - Since migration `2024_11_30_075826`, piggy banks can link to multiple accounts

### User Group / Multi-Tenancy Rules
**Source:** `app/Models/Account.php:147`, multiple model files

1. **User Group Isolation** - All financial data is scoped to user groups:
   - Accounts have `user_group_id` foreign key
   - Transactions inherit user group from accounts
   - Rules, budgets, categories are user-group scoped

2. **Single User Mode** - System can operate in single-user mode (`config/firefly.php:52`)

### Currency Exchange Rules
**Source:** `app/Console/Commands/Tools/Cron.php:115-129`

1. **Exchange Rate Download** - System can automatically download exchange rates:
   - Configurable via `enable_external_rates` setting
   - Runs on cron schedule
   - Stores historical rates for conversions

2. **Native Amount Calculation** - All transactions store native amounts in primary currency for reporting

### Data Integrity Rules

1. **Soft Deletes** - All major entities use soft deletes:
   - Accounts, Transactions, Bills, Budgets, Categories, Rules, Tags (`app/Models/*.php`)
   - Preserves audit trail and prevents data loss

2. **Cascade Deletes** - Foreign key relationships enforce cascading:
   - User deletion removes all financial data
   - Account deletion removes transactions
   - Transaction journal deletion removes splits

3. **Encryption Support** - Fields can be marked as encrypted (`app/Models/Account.php:62`, `app/Models/Bill.php:101`)

## Data Flows

### Transaction Creation Flow
**Source:** `app/Services/Internal/Update/GroupUpdateService.php`

1. **Input**: User submits transaction data via API or web form
2. **Validation**: System validates account types, amounts, dates
3. **Group Creation**: TransactionGroup created (split or single)
4. **Journal Creation**: TransactionJournal(s) created with metadata
5. **Split Creation**: Individual Transaction records created (source + destination)
6. **Linking**: Attach budgets, categories, tags, bills
7. **Event Firing**: Audit log entries created
8. **Balance Update**: Account balances recalculated (async)
9. **Rule Application**: Matching rules executed (if enabled)

### Import Flow
**Source:** Inferred from API routes and config

1. **File Upload**: CSV/OFX/etc uploaded via API
2. **Parsing**: External tool or internal parser reads transactions
3. **Duplicate Detection**: `import_hash` and `import_hash_v2` fields prevent duplicates (`config/firefly.php:554`)
4. **Mapping**: User maps file columns to Firefly fields
5. **Account Matching**: Auto-match or create accounts
6. **Import Execution**: Transactions created via TransactionFactory
7. **Rule Application**: Rules applied to imported transactions

### Report Generation Flow
**Source:** API routes in `routes/api.php:164-292`

1. **Parameters**: User selects accounts, date range, report type
2. **Query Building**: GroupCollector builds optimized queries
3. **Data Aggregation**: Sum amounts by category/budget/tag
4. **Chart Generation**: ChartJS data structures created
5. **Export**: Data can be exported as CSV/JSON

### Cron Job Flow
**Source:** `app/Console/Commands/Tools/Cron.php`

1. **Scheduled Execution**: Cron runs daily (configurable)
2. **Exchange Rates**: Download latest rates if enabled
3. **Version Check**: Check for Firefly III updates
4. **Recurring Transactions**: Create due recurring transactions
5. **Auto-Budgets**: Create budget limits for new periods
6. **Bill Warnings**: Send email notifications for upcoming bills
7. **Webhook Messages**: Send queued webhook messages (max 5 per run)

### Balance Calculation Flow
**Source:** Migration `2024_07_28_145631_add_running_balance.php`

1. **Transaction Saved**: Observer triggers balance recalculation
2. **Running Balance**: Calculate cumulative balance per account
3. **Period Statistics**: Aggregate statistics stored in `period_statistics` table
4. **Cache Update**: Balance cache invalidated
5. **Display**: Dashboard and account views show current balances

## Integration Points

### External APIs

1. **Exchange Rate Providers** (Configurable):
   - **Fixer.io** API (`config/firefly.php:105` - `FIXER_API_KEY`)
   - Supports other providers via configuration
   - Used for automatic currency conversion

2. **Version Check** (`config/firefly.php:122`):
   - **Endpoint**: `https://version.firefly-iii.org/index.json`
   - Frequency: Every 7 days minimum
   - Returns available updates

3. **IP Geolocation** (`config/firefly.php:106`):
   - **IPInfo API** (`IPINFO_TOKEN`)
   - Used for security alerts (new login locations)

### Authentication Integrations

**Source:** `.env.example:240-247`, `config/firefly.php:137-139`

1. **Laravel Passport** - OAuth2 server for API authentication:
   - Token-based authentication
   - Personal access tokens
   - OAuth clients for third-party apps

2. **Remote User Guard** (`AUTHENTICATION_GUARD=remote_user_guard`):
   - **Authelia** support
   - Header-based authentication (`AUTHENTICATION_GUARD_HEADER=REMOTE_USER`)
   - Reverse proxy authentication

3. **2FA (Two-Factor Authentication)**:
   - **Google Authenticator** compatible
   - Backup codes for recovery
   - Uses `pragmarx/google2fa` package

### Email Integrations

**Source:** `.env.example:154-177`

1. **SMTP** - Standard email delivery
2. **Mailgun** - Cloud email service (`MAILGUN_DOMAIN`, `MAILGUN_SECRET`)
3. **Mandrill** - Transactional email (`MANDRILL_SECRET`)
4. **SparkPost** - Email delivery (`SPARKPOST_SECRET`)
5. **MailerSend** - Email API (`MAILERSEND_API_KEY`)

### Notification Channels

**Source:** `app/Notifications/` directory structure

1. **Email** - All notification types supported
2. **Slack** - Webhook-based notifications
3. **Pushover** - Mobile push notifications
4. **Ntfy** - Self-hosted push notifications

### Webhook System

**Source:** `routes/api.php:804-831`, `config/firefly.php:581-583`

1. **Webhook Configuration**:
   - User-defined endpoint URLs
   - Trigger events (transaction created/updated/deleted)
   - Delivery methods (immediate/queued)
   - Response validation
   - Signature verification (SHA3)

2. **Webhook Messages**:
   - Stored in `webhook_messages` table
   - Retry attempts tracked (`max_attempts` = 3)
   - Cron job processes queued messages

### Database Support

**Source:** `config/database.php`

1. **MySQL/MariaDB** - Primary support with SSL options:
   - SSL certificate verification
   - Custom CA paths
   - Cipher configuration

2. **PostgreSQL** - Full support with schema configuration:
   - SSL mode (prefer/require/disable)
   - Custom schema support (default: public)
   - Certificate-based auth

3. **SQLite** - Supported for development/testing

### Cache & Session Stores

**Source:** `.env.example:128-146`, `config/database.php:111-140`

1. **Redis** - Preferred for production:
   - Session storage
   - Cache storage
   - Separate databases for each (`REDIS_DB`, `REDIS_CACHE_DB`)
   - TCP and Unix socket support
   - ACL support (Redis 6+)

2. **File-based** - Default fallback

### Import/Export Integrations

**Source:** API routes `routes/api.php:150-161`

1. **Export Formats**:
   - CSV with customizable fields
   - JSON API format
   - Supports all entity types (accounts, transactions, budgets, etc.)

2. **Import Sources**:
   - CSV files
   - Integration with external data import tools (via API)
   - Duplicate detection via hash fields

### Third-Party Apps Ecosystem

**Source:** `readme.md:82-84`

Multiple community tools integrate via API:
- Mobile apps (Android, iOS)
- Data importers (Plaid, bank CSVs)
- Browser extensions

## Constraints

### Technical Constraints

1. **PHP Version**: Requires PHP 8.5+ (`composer.json:43`)
2. **PHP Extensions Required** (`composer.json:44-58`):
   - bcmath, curl, fileinfo, iconv, intl, json
   - mbstring, openssl, pdo, session, simplexml
   - sodium, tokenizer, xml, xmlwriter

3. **Laravel Version**: Laravel 12 framework (`composer.json:71`)

4. **Database Schema Version**: Current version 28 (`config/firefly.php:99`)

5. **File Upload Limits**: Max 1GB per upload (`config/firefly.php:102`)

6. **Decimal Precision**: Financial amounts use 32 digits, 12 decimal places (`database/migrations/2016_06_16_000002_create_main_tables.php:180`)

### Licensing Constraints

**Source:** `composer.json:11`, `LICENSE`

1. **License**: GNU AGPL-3.0-or-later
   - Open source, copyleft license
   - Requires source code disclosure for hosted instances
   - Prohibits proprietary forks without source release

### Security Constraints

1. **Self-Hosted Only**: No cloud/SaaS offering by design
2. **Data Sovereignty**: All data remains on user's infrastructure
3. **HTTPS Required**: Recommended for production (`COOKIE_SECURE=false` default)
4. **CSRF Protection**: Enabled by default (`app/Http/Middleware/VerifyCsrfToken.php`)
5. **CSP Headers**: Content Security Policy enforced (can be disabled via env)

### Operational Constraints

1. **Cron Dependency**: Critical features require scheduled tasks:
   - Recurring transactions won't fire without cron
   - Bills won't trigger notifications
   - Exchange rates won't update
   - Auto-budgets won't create

2. **Timezone Handling**: Separate timezone storage for dates:
   - `date_tz` field stores original timezone
   - System converts for display/calculation
   - Introduced in migration `2024_11_05_062108`

3. **Multi-Currency Complexity**:
   - Native amounts always calculated
   - Exchange rates must be available
   - Manual rate entry fallback required

### Data Constraints

1. **User Group Isolation**: Strict data separation between user groups
2. **Soft Delete Overhead**: Deleted records remain in database
3. **Audit Log Growth**: Every change logged for history (`audit_log_entries` table)
4. **Balance Cache**: Running balances stored for performance (migration `2024_07_28_145631`)

### Browser Support

**Source:** `public/v1/js/lib/modernizr-custom.js`

1. Requires modern JavaScript (ES6+)
2. No Internet Explorer support implied
3. Progressive web app capabilities (PWA manifest)

## Key Workflows

### 1. New User Onboarding
**Entry Point:** `routes/web.php:369-373`

1. User registers account
2. Primary currency selection
3. Asset account creation (checking/savings)
4. Optional initial balance setup
5. Dashboard tour (intro.js)

### 2. Daily Transaction Recording
**Entry Point:** `app/Http/Controllers/Transaction/CreateController.php`

1. User navigates to transaction creation
2. Selects transaction type (withdrawal/deposit/transfer)
3. System filters account options based on type
4. User enters amount, description, date
5. Optional: category, budget, tags assignment
6. System validates and creates transaction group
7. Rules automatically applied (if configured)
8. Balance updated, audit log created

### 3. Bill Management Lifecycle
**Entry Point:** `app/Http/Controllers/Bill/`

1. **Create**: User defines bill with amount range, frequency, matching patterns
2. **Auto-match**: System scans transactions for matches
3. **Review**: User confirms/rejects matches
4. **Payment Tracking**: Linked transactions mark bill as paid
5. **Notification**: Cron job sends reminder before due date
6. **Historical View**: Dashboard shows payment history

### 4. Budget Planning & Tracking
**Entry Point:** `app/Http/Controllers/Budget/`

1. **Setup**: User creates budget categories
2. **Allocation**: Define amount limits per period
3. **Auto-budget** (optional): System creates limits automatically
4. **Transaction Assignment**: Withdrawals linked to budgets (manual or rules)
5. **Monitoring**: Dashboard shows spent vs. budgeted
6. **Alerts**: Warnings when approaching/exceeding limits

### 5. Monthly Reconciliation
**Entry Point:** `app/Http/Controllers/Account/ReconcileController.php`

1. User selects account and date range
2. Enters expected ending balance (from bank statement)
3. System calculates difference
4. User reviews unreconciled transactions
5. Marks transactions as reconciled
6. System creates reconciliation transaction if needed
7. Balance discrepancy resolved

### 6. Report Generation
**Entry Point:** `app/Http/Controllers/ReportController.php`

**Report Types:**
- **Default**: Income vs. expenses over time
- **Audit**: All transactions with full details
- **Budget**: Spending per budget category
- **Category**: Spending per category
- **Tag**: Spending per tag
- **Double**: Expense/revenue account analysis

**Workflow:**
1. User selects report type and parameters
2. Date range, accounts, optional filters
3. System generates charts and tables
4. Export to CSV/JSON available
5. Report can be saved for repeated access

### 7. Recurring Transaction Management
**Entry Point:** `app/Http/Controllers/Recurring/`

1. **Create**: User defines transaction template
2. **Schedule**: Set first date, frequency, end date
3. **Activation**: Enable recurring transaction
4. **Cron Execution**: System creates transactions on schedule
5. **Rule Application** (optional): Apply rules to created transactions
6. **Manual Trigger**: User can force creation
7. **History**: View all created transaction instances

### 8. Rule-Based Automation
**Entry Point:** `app/Http/Controllers/Rule/`

**Decision Branches:**
- **Trigger Matching**: AND (strict) vs. OR (loose) modes
- **Stop Processing**: Halt rule execution after match
- **Group Execution**: Process entire rule group at once

**Workflow:**
1. User creates rule with triggers (conditions)
2. Defines actions (modifications)
3. Sets execution order
4. System applies to new transactions automatically
5. Or: User selects transactions and applies manually
6. Test mode: Preview matches before applying

## Domain Entities

### Core Financial Entities

1. **Account** (`app/Models/Account.php`)
   - **Attributes**: name, type, IBAN, balance, active, virtual_balance
   - **Types**: Asset, Expense, Revenue, Liability (Loan/Debt/Mortgage)
   - **Relationships**: User, UserGroup, AccountType, Transactions, PiggyBanks, ObjectGroups
   - **Lifecycle States**: active/inactive, encrypted, deleted
   - **Invariants**: Type determines allowed transactions; virtual balance only for assets

2. **TransactionGroup** (`app/Models/TransactionGroup.php`)
   - **Attributes**: title, user_id, user_group_id
   - **Relationships**: TransactionJournals (one-to-many)
   - **Purpose**: Groups split transactions

3. **TransactionJournal** (`app/Models/TransactionJournal.php`)
   - **Attributes**: description, date, type, bill_id, currency, completed
   - **Relationships**: Transactions (splits), Budgets, Categories, Tags, Bill, TransactionGroup
   - **Lifecycle States**: completed/incomplete
   - **Invariants**: Must have at least 2 transactions (source + destination)

4. **Transaction** (`app/Models/Transaction.php`)
   - **Attributes**: amount, foreign_amount, account_id, description, reconciled
   - **Relationships**: Account, TransactionJournal, Currency, ForeignCurrency
   - **Invariants**: Amount must balance across journal; reconciled transactions locked

5. **Budget** (`app/Models/Budget.php`)
   - **Attributes**: name, active, order
   - **Relationships**: BudgetLimits, TransactionJournals, AutoBudgets
   - **Lifecycle States**: active/inactive
   - **Invariants**: Only assigned to withdrawals

6. **BudgetLimit** (referenced in models)
   - **Attributes**: amount, start_date, end_date, budget_id
   - **Relationships**: Budget
   - **Purpose**: Time-based budget constraints

7. **Category** (`app/Models/Category.php`)
   - **Attributes**: name, encrypted
   - **Relationships**: TransactionJournals, Transactions
   - **Purpose**: Transaction classification

8. **Bill** (`app/Models/Bill.php`)
   - **Attributes**: name, match_pattern, amount_min, amount_max, date, repeat_freq, skip, automatch
   - **Relationships**: TransactionJournals
   - **Lifecycle States**: active/inactive, matched/unmatched
   - **Invariants**: Amount range defines valid matches

9. **PiggyBank** (referenced in config)
   - **Attributes**: name, target_amount, start_date, target_date
   - **Relationships**: Accounts (many-to-many since Nov 2024), PiggyBankEvents
   - **Purpose**: Savings goal tracking

10. **Tag** (referenced in config)
    - **Attributes**: tag, description, date, location (lat/long)
    - **Relationships**: TransactionJournals
    - **Purpose**: Flexible transaction labeling

11. **Rule** (`app/Models/Rule.php`)
    - **Attributes**: title, description, active, strict, order, stop_processing
    - **Relationships**: RuleGroup, RuleTriggers, RuleActions
    - **Lifecycle States**: active/inactive
    - **Execution**: Sequential by order within group

12. **RuleGroup** (referenced in models)
    - **Attributes**: title, description, active, order
    - **Relationships**: Rules
    - **Purpose**: Organize rules into logical collections

13. **Recurrence** (`app/Models/Recurrence.php`)
    - **Attributes**: title, first_date, repeat_until, repetitions, apply_rules, active
    - **Relationships**: RecurrenceTransactions, RecurrenceRepetitions, RecurrenceMeta
    - **Lifecycle States**: active/inactive, has_fired
    - **Invariants**: Either repeat_until or repetitions must be set

### Supporting Entities

14. **TransactionCurrency** (referenced in models)
    - **Attributes**: code, name, symbol, enabled
    - **Purpose**: Multi-currency support

15. **UserGroup** (referenced in models)
    - **Attributes**: title
    - **Relationships**: Users, all financial entities
    - **Purpose**: Multi-tenancy/family accounts

16. **Attachment** (referenced in config)
    - **Polymorphic**: Can attach to Account, Bill, Budget, Category, Transaction, etc.
    - **Attributes**: filename, mime, size, md5
    - **Purpose**: Store receipts, documents

17. **Webhook** (referenced in API routes)
    - **Attributes**: url, trigger, delivery, response, active
    - **Relationships**: WebhookMessages, WebhookAttempts
    - **Purpose**: External system integration

18. **AuditLogEntry** (referenced in models)
    - **Polymorphic**: Links to any auditable entity
    - **Attributes**: before, after, action_name
    - **Purpose**: Complete audit trail

## Assumptions

1. **Assumption**: Users have basic financial literacy (understand debits/credits, budgeting concepts)
   - **Inference**: Based on double-entry system and accounting terminology

2. **Assumption**: Users manage personal finances, not business accounting
   - **Evidence**: No invoicing, accounts receivable/payable, or tax features

3. **Assumption**: Users prefer self-hosting over cloud SaaS
   - **Evidence**: Explicit design goal in readme.md, no hosted option provided

4. **Assumption**: Daily cron execution is acceptable for all automated tasks
   - **Evidence**: No real-time webhook or event streaming, all automation via cron

5. **Assumption**: Users want complete control and transparency
   - **Evidence**: Open source, audit logs, self-hosted emphasis

6. **Assumption**: Mobile app users will use third-party integrations
   - **Evidence**: No native mobile app, but API supports ecosystem

7. **Assumption**: Exchange rates update daily is sufficient
   - **Evidence**: Cron-based download, no intraday rate tracking

8. **Assumption**: Users primarily manage one or few currencies
   - **Evidence**: Primary currency concept, native amount always calculated

9. **Assumption**: Reconciliation is monthly or less frequent
   - **Evidence**: Manual process, no real-time bank feeds

10. **Assumption**: Users trust local infrastructure security
    - **Evidence**: Self-hosted with minimal cloud dependencies

## Open Questions

### Business Logic Clarifications

1. **What happens when a user changes primary currency mid-year?**
   - How are historical native amounts recalculated?
   - Are reports consistent across the change boundary?

2. **How are split transactions reconciled?**
   - Does each split need individual reconciliation?
   - Or does reconciling the group reconcile all splits?

3. **What is the exact duplicate detection algorithm?**
   - How is `import_hash` calculated?
   - How does `import_hash_v2` differ?
   - What fields are included?

4. **How do auto-budgets calculate amounts?**
   - Based on previous period spending?
   - Fixed amounts?
   - Configurable formula?

5. **What triggers balance recalculation?**
   - Only transaction CRUD?
   - Account changes?
   - Currency updates?
   - Full rebuild process?

6. **How are recurring transaction conflicts handled?**
   - If cron fails to run, are missed transactions created retroactively?
   - Or are they skipped?

7. **What is the piggy bank event model exactly?**
   - How are multi-account piggy banks divided?
   - Who decides which account to use for a contribution?

### Technical Debt & Modernization Concerns

8. **Why are there two parallel UI implementations (v1 and v2)?**
   - `resources/views/v2/` and `resources/assets/v2/`
   - What is the migration path?
   - Which is production-ready?

9. **What is the status of the expression engine feature?**
   - `feature_flags.expression_engine = true`
   - Where is it used?
   - What expressions are supported?

10. **Why is telemetry feature flagged but disabled?**
    - `feature_flags.telemetry = false`
    - What would it collect?
    - Privacy implications?

11. **What is the "domain/" directory in the autoload path?**
    - `composer.json:88` - `"Domain\\": "domain/"`
    - Domain-driven design refactor in progress?
    - Not visible in file listing

12. **What are the "period_statistics" used for?**
    - Migration `2025_09_25_175248_create_period_statistics.php`
    - Performance optimization?
    - Pre-aggregated reports?

13. **How is the running balance feature implemented?**
    - Migration `2024_07_28_145631_add_running_balance.php`
    - Real-time or calculated on-demand?
    - Impact on large transaction volumes?

### Integration & Security Questions

14. **What is the OAuth client auto-creation event for?**
    - `app/Mail/OAuthTokenCreatedMail.php`
    - Security concern or legitimate use case?

15. **How are webhook signatures generated and validated?**
    - SHA3 mentioned in `app/Helpers/Webhook/Sha3SignatureGenerator.php`
    - What data is signed?
    - How do receivers validate?

16. **What notification channels actually work?**
    - Ntfy support added recently
    - Are Pushover/Slack fully implemented?
    - Test coverage?

17. **How does the remote user guard work in practice?**
    - Reverse proxy setup required?
    - Compatible with Traefik, Nginx?
    - Security risks if misconfigured?

### Performance & Scalability Questions

18. **What are the performance limits?**
    - Max transactions per account?
    - Max accounts per user?
    - Max split size?

19. **How does soft delete impact query performance over time?**
    - Are deleted records filtered in all queries?
    - Database growth implications?

20. **Is the balance cache automatically maintained?**
    - Or does it require manual refresh?
    - Cache invalidation strategy?

21. **What happens if exchange rate API is unavailable?**
    - Fallback strategy?
    - Manual rate entry UI?
    - Impact on reporting?

### Data Migration Questions

22. **How are users notified of breaking schema changes?**
    - Upgrade guides?
    - Automated migration testing?

23. **Is there a rollback strategy for failed upgrades?**
    - Database backup before migration?
    - Downgrade path?

24. **How long is audit log retention?**
    - Configurable?
    - Performance impact of large audit tables?

---

**Document Version:** 1.0  
**Generated:** 2025-01-21  
**Source Codebase:** Firefly III v6.5.9 (https://github.com/firefly-iii/firefly-iii)  
**Analysis Scope:** 1457 PHP files, 264 Twig templates, 42 Vue components