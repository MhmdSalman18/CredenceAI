# Development setup

## VS Code

1. Open this project folder in VS Code.
2. Install the recommended Flutter and Dart extensions (Dart Code).
3. Flutter should resolve from PATH. If prompted, select the SDK folder containing `bin`.
4. Run `flutter pub get` in the integrated terminal.
5. Select an Android phone or emulator from the status bar, then press F5 with the **Credence AI** launch configuration.

The app targets Android API 24+. A Windows build is included for UI preview. Windows native plugin links require Developer Mode. The Android notification listener and Google backup sign-in are Android-specific. Web and iOS targets are not included.

If Windows says plugins need symlink support, enable Developer Mode in Windows Settings and run `flutter pub get` again. This is separate from Android USB debugging.

## Android notification tracking

Open Settings inside the app, choose Notification access, read the explanation and enable access for **Credence AI Flutter** in Android settings. Keep Automatic transaction tracking enabled.

Only new notifications are processed; the app does not request SMS inbox access or import historical messages. Some phone manufacturers restrict background services. Validate on your actual phone. Notification history shows detected transactions.

## Local data and backups

No account is required for transactions, budgets, analytics, CSV export or encrypted file backups. The app begins empty and does not access the Compose app's sandbox.

Backups require a passphrase of at least ten characters. There is no recovery key. Restore replaces local transactions and budgets after decrypting and validating the whole backup. Profile/theme/privacy preferences remain device-specific and are not included. Compose binary database backups use a different format and are not accepted.

## Optional Firebase and Google setup

1. Register a Firebase Android app with package **com.credenceai.credence_ai_flutter**. The separate package allows side-by-side installation with Compose.
2. Enable Firebase Authentication's Google provider and Firebase Storage.
3. Register debug and release SHA-1/SHA-256 signing fingerprints for the new Android OAuth client. Run `.\gradlew signingReport` from `android` to obtain them.
4. Obtain the Web OAuth client ID used as `GOOGLE_SERVER_CLIENT_ID`.
5. For Drive backup, enable Google Drive API and configure the OAuth consent screen for the `drive.appdata` scope, including test users where required.
6. Copy `config/example.json` to `config/local.json` and fill in the Firebase client configuration and OAuth ID. No Gemini secret belongs in this file.
7. Review `firebase/storage.rules` and apply its owner-scoped rules to the intended bucket through your usual deployment workflow. These rules have not been deployed. Merge them with existing rules instead of replacing unrelated access rules.
8. Use the **Credence AI (configured services)** launch configuration or `flutter run --dart-define-from-file=config/local.json`.

Firebase is initialized explicitly from these values. No Google Services Gradle plugin or copied Compose `google-services.json` is required. OAuth still needs correct package/signing registration. Cloud operations need testing against your account after configuration.

## Crence backend

`server` contains a Node.js proxy that keeps Gemini keys outside the mobile application.

1. Use Node.js 22+ and run `npm ci` inside `server`.
2. Copy `.env.example` to `.env`. Configure a Gemini API key, an available model ID and the Firebase project ID. No retired model is hard-coded.
3. Supply Firebase Admin credentials using workload identity or Application Default Credentials. Keep service-account files outside this repository.
4. Run `npm start`. The service binds to loopback port 8080. Use an HTTPS reverse proxy, or deliberately adjust the binding for your managed hosting environment.
5. Set the Flutter `CHAT_ENDPOINT` to `https://your-host/chat`, rebuild, and sign in with Google in the app.

The backend verifies Firebase ID tokens and applies per-process account rate limits, body/history limits and upstream timeouts. Configure shared rate limits, quotas, secret management and monitoring for a multi-instance deployment. Nothing is deployed automatically. Chat sends only typed messages; transaction records are not attached.

Protocol: `POST /chat`, `Authorization: Bearer <Firebase ID token>`, body `{ "messages": [{ "role": "user", "text": "Budgeting tips" }] }`. Success returns `{ "text": "..." }`; errors use 400/401/413/415/429/502 with generic messages.

References: [Gemini generateContent](https://ai.google.dev/api/generate-content), [Firebase ID-token verification](https://firebase.google.com/docs/auth/admin/verify-id-tokens).

## Checks

```powershell
dart format --output=none --set-exit-if-changed lib test
flutter analyze
flutter test
flutter build apk --debug
cd android
.\gradlew testDebugUnitTest
```

Backend: run `npm test` and `node --check src/server.mjs` from `server`.

## Release signing

Create an upload keystore outside the repository. Add ignored `android/key.properties`:

```properties
storeFile=C:/path/to/upload-keystore.jks
storePassword=your-password
keyAlias=your-alias
keyPassword=your-password
```

Release builds fail until signing is configured. Debug builds use Android's usual debug key. Build a signed bundle with `flutter build appbundle --release --dart-define-from-file=config/local.json`.

## Manual acceptance

- Add, edit and delete expense/income; confirm persistence after restart.
- Search/filter history and export merchants containing commas/quotes.
- Create an equally allocated budget with an indivisible minor-unit total.
- Change months/currencies and check totals remain separate.
- Save/restore backup and confirm wrong passwords preserve existing records.
- Enable app lock, background and verify device authentication.
- On Android, test supported debit/credit notifications and OTP rejection.
- With services configured, test Google sign-in, both cloud destinations and chat network failures.
