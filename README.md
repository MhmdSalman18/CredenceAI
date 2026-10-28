# Credence AI Flutter

A Flutter recreation of the CredenceAI Jetpack Compose Android application, organized for maintainable feature development. The Compose source remains in its original project folder.

## Run in VS Code

Install the recommended Flutter extension, open this folder and select an Android emulator or phone. Run:

```powershell
flutter pub get
flutter run
```

Or press **F5** using the **Credence AI** launch configuration. Windows preview is included; enable Windows Developer Mode for native plugin symbolic links. Notification tracking and Google cloud sign-in require Android. Web/iOS targets are not included.

## Features

- Dashboard with monthly income, expenses, balance, recent activity and categories.
- Add, edit and delete transactions, including date/time, category, notes and payment method.
- Search and filter history, review uncategorized records, and export CSV.
- Monthly budgets, precise category allocation, recurring limits and progress.
- Monthly analytics with category and daily-spending charts.
- Profile, dark theme, currency-separated totals, hidden balance and device authentication.
- Android bank/payment notification detection with a persistent native inbox and idempotent import.
- Encrypted local backup/restore; Firebase and Google Drive adapters after configuration.
- Crence chat with an authenticated backend adapter and included Node service. Live AI requires service configuration and deployment.

The app starts with no sample financial data. Existing Compose app data is not automatically migrated. Compose binary database backups are not compatible with the Flutter backup format. Settings that existed only as nonfunctional toggles in the original are not presented as working features here.

## Code structure

```text
lib/
  app/                  App shell, routing and lifecycle
  core/                 Shared configuration, storage, money, theme and widgets
  features/
    transactions/       Domain, repository, controller and screens
    budgets/            Domain, repository, controller and screens
    dashboard/          Monthly overview
    analytics/          Calculations and charts
    notifications/      Flutter-to-Android adapter
    settings/           Preferences and privacy
    backup/             Export, encryption, restore and cloud adapters
    chat/               Conversation state and authenticated HTTP client
android/app/src/main/kotlin/.../notifications/
                        Native parser, background service and durable inbox
server/                 Firebase-authenticated Gemini proxy
config/                 Example local build configuration
firebase/               Example owner-scoped Storage rules
```

Flutter + Riverpod + GoRouter; SQLite repositories behind domain interfaces; integer minor units for money. Encryption uses AES-256-GCM and PBKDF2. No Gemini API secret is embedded in the application.

## Quality checks

```powershell
dart format --output=none --set-exit-if-changed lib test
flutter analyze
flutter test
flutter build apk --debug
```

Run `./gradlew testDebugUnitTest` inside `android` for parser tests and `npm test` inside `server` for request validation tests. The included CI workflow runs these checks. Release signing must be configured explicitly; release builds never fall back to the debug key.

## Documentation

- [Development, services, signing and manual acceptance](docs/SETUP.md)
- [Architecture, data flow, backup format and security limits](docs/ARCHITECTURE.md)

Cloud account integration, paid-service configuration, deploying the backend/rules and release publishing are separate setup steps. Nothing has been deployed or published.
