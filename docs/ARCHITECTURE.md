# Architecture

Credence AI is a feature-organized Flutter app with an Android notification adapter.

## Dependency boundaries

- `lib/app`: bootstrap integration, routing, lifecycle, app lock and application shell.
- `lib/core`: shared storage, DI, money formatting, configuration, errors, themes and widgets.
- `lib/features/<feature>/domain`: Dart entities, calculation rules and repository interfaces.
- `lib/features/<feature>/data`: SQLite, HTTP or platform implementations.
- `lib/features/<feature>/application`: Riverpod controllers and application services.
- `lib/features/<feature>/presentation`: screens, forms and view-specific state.

Domain entities never import Flutter, SQL, HTTP or Riverpod. Views do not issue SQL or perform network calls. Repository interfaces are injected by Riverpod providers. Transient form controls stay in their screen; persistent state changes go through controllers and repositories.

## Persistence

SQLite schema v1 stores money as integer minor units and timestamps as epoch milliseconds. Transactions have stable IDs and a unique optional notification reference. Repeated delivery is idempotent. Currency is stored on each record; summaries never combine currencies. Amounts are not converted when the display currency changes.

Budget allocations use the same category names as transactions. Equal allocation preserves remainder cents exactly. Recurring budgets start in their selected month. Their spending is recalculated for the displayed month. Multiple active budgets are independent limits; the dashboard displays their combined total.

Schema upgrades require explicit migrations in `AppDatabase`; there is no destructive fallback. Database startup errors offer a retry and never erase data.

## Notification ingestion

The Android `NotificationListenerService` filters a package allowlist, recognised bank SMS senders, OTPs and promotional messages. A pure Kotlin parser extracts amount, direction, currency and merchant. The service writes parsed records into a separate SQLite inbox even when the Flutter engine is absent. It does not persist full SMS bodies.

An EventChannel signals foreground activity. Flutter drains the inbox on launch, resume and new events. It saves each transaction before acknowledging the inbox entry. Unique notification references protect against retries after a crash. Entries stay pending on import failure. Android settings provide explicit user consent; the service honours the persisted tracking toggle.

Notification formats differ across providers. Detection remains best-effort and needs real-device verification. Messages with no visible amount/direction or unsupported senders are ignored. Force-stopping an Android app can prevent service delivery until it is opened again.

## Backups and account integrations

Backups are versioned JSON encrypted with AES-256-GCM and a PBKDF2-HMAC-SHA256 key (210,000 iterations, random 16-byte salt and 12-byte nonce). Key derivation runs in a separate isolate. Passphrases are neither stored nor sent to cloud providers. Authenticated decryption and complete structural validation precede a single transactional database replacement. Wrong passwords and corrupt files leave records intact.

Local, Firebase Storage and Google Drive backups use the same encrypted bytes. Cloud integration is enabled through local build configuration; Firebase and Google sign-in must be registered for the Flutter app's separate Android application ID. The old Compose configuration is not silently reused.

Chat goes through an authenticated HTTPS backend. Gemini credentials remain on the backend. Only typed chat messages are sent; transactions are not attached. Conversations remain in memory and can be cleared. The included server verifies Firebase ID tokens, limits request size/history, times out upstream requests and rate-limits per account in one process. Multi-instance deployments need a shared rate limiter and deployment-level request limits.

## Security limits

App lock uses device authentication and gates the interface when the app is backgrounded. It is not database encryption. Android OS application storage isolation protects the live local SQLite database; encrypted exports protect backup files. CSV exports are plaintext user-requested reports and escape spreadsheet formula prefixes.

There is no analytics SDK or logging of notification content. The app has no direct Gemini API key. Build configuration and signing files are ignored by Git. Firebase client configuration identifies the app and is not an authorization boundary: use restrictive server/storage rules and OAuth package/signing registration.

## Quality gates

Run formatter, analyzer, Flutter tests, Android parser unit tests and a debug APK build. The CI workflow performs the same checks. Automated tests cover month/currency boundaries, cent allocation, duplicate delivery, encryption integrity, failed restore preservation and phone-size form rendering.
