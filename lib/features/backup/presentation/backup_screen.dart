import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/di/dependencies.dart';
import '../../../core/errors/app_exception.dart';
import '../../../core/widgets/common.dart';
import '../../budgets/application/budgets_controller.dart';
import '../../transactions/application/transactions_controller.dart';
import '../application/backup_service.dart';
import '../data/backup_cipher.dart';
import '../data/cloud_backup_service.dart';

class BackupScreen extends ConsumerStatefulWidget {
  const BackupScreen({super.key});
  @override
  ConsumerState<BackupScreen> createState() => _BackupScreenState();
}

class _BackupScreenState extends ConsumerState<BackupScreen> {
  final password = TextEditingController();
  final confirmation = TextEditingController();
  bool busy = false;
  CloudDestination destination = CloudDestination.firebase;
  String? message;
  @override
  void dispose() {
    password.dispose();
    confirmation.dispose();
    super.dispose();
  }

  Future<void> run(Future<String?> Function() action) async {
    setState(() {
      busy = true;
      message = null;
    });
    try {
      final result = await action();
      if (mounted) setState(() => message = result);
    } catch (e) {
      if (mounted) showError(context, e);
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  void validatePassword() {
    if (password.text.length < 10) {
      throw const AppException('Use a passphrase of at least 10 characters.');
    }
    if (password.text != confirmation.text) {
      throw const AppException('The passphrases do not match.');
    }
  }

  Future<bool> confirmRestore() => confirmAction(
    context,
    title: 'Replace your records?',
    message: 'Restore replaces all transactions and budgets on this device. Create a backup of your current records first.',
    action: 'Restore',
  );

  void refreshed() {
    ref.invalidate(transactionsProvider);
    ref.invalidate(budgetsProvider);
  }

  @override
  Widget build(BuildContext context) {
    final cloud = ref.watch(cloudBackupProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Backup & restore')),
      body: PageBody(
        children: [
          const EmptyState(
            title: 'Your backup, protected',
            message: 'Backups are encrypted on your device with your passphrase. Keep it safe: it cannot be recovered.',
            icon: Icons.enhanced_encryption_outlined,
          ),
          const SizedBox(height: 20),
          TextField(
            controller: password,
            obscureText: true,
            autocorrect: false,
            enableSuggestions: false,
            decoration: const InputDecoration(
              labelText: 'Passphrase (10+ characters)',
            ),
          ),
          const SizedBox(height: 14),
          TextField(
            controller: confirmation,
            obscureText: true,
            autocorrect: false,
            enableSuggestions: false,
            decoration: const InputDecoration(
              labelText: 'Confirm passphrase for new backups',
            ),
          ),
          const SectionTitle('On this device'),
          FilledButton.icon(
            onPressed: busy
                ? null
                : () => run(() async {
                    validatePassword();
                    final bytes = await ref
                        .read(backupServiceProvider)
                        .create(password.text);
                    final saved = await FilePicker.saveFile(
                      fileName:
                          'credence_${DateTime.now().millisecondsSinceEpoch}.enc',
                      bytes: bytes,
                    );
                    if (saved == null) return null;
                    await ref
                        .read(preferencesProvider)
                        .setString(
                          'last_backup',
                          DateTime.now().toIso8601String(),
                        );
                    return 'Encrypted backup saved.';
                  }),
            icon: const Icon(Icons.save_alt),
            label: const Text('Save encrypted backup'),
          ),
          const SizedBox(height: 12),
          OutlinedButton.icon(
            onPressed: busy
                ? null
                : () async {
                    if (!await confirmRestore()) return;
                    await run(() async {
                      final picked = await FilePicker.pickFile();
                      if (picked == null) return null;
                      if ((await picked.length() ?? 0) >
                          BackupCipher.maxBytes) {
                        throw const AppException('This backup is too large.');
                      }
                      final bytes = await picked.xFile.readAsBytes();
                      await ref
                          .read(backupServiceProvider)
                          .restore(bytes, password.text);
                      refreshed();
                      return 'Transactions and budgets restored.';
                    });
                  },
            icon: const Icon(Icons.restore),
            label: const Text('Restore from file'),
          ),
          const SectionTitle('Cloud backup'),
          if (!cloud.configured)
            const Card(
              child: Padding(
                padding: EdgeInsets.all(18),
                child: Text(
                  'Cloud backup needs Firebase and Google sign-in configuration for this app. Local backup works without an account.',
                ),
              ),
            )
          else ...[
            if (cloud.email == null)
              OutlinedButton.icon(
                onPressed: busy
                    ? null
                    : () => run(() async {
                        await cloud.signIn();
                        return 'Signed in with Google.';
                      }),
                icon: const Icon(Icons.account_circle_outlined),
                label: const Text('Sign in with Google'),
              )
            else
              ListTile(
                title: Text(cloud.email!),
                trailing: TextButton(
                  onPressed: busy
                      ? null
                      : () => run(() async {
                          await cloud.signOut();
                          return 'Signed out.';
                        }),
                  child: const Text('Sign out'),
                ),
              ),
            const SizedBox(height: 12),
            SegmentedButton<CloudDestination>(
              segments: const [
                ButtonSegment(
                  value: CloudDestination.firebase,
                  label: Text('Firebase'),
                ),
                ButtonSegment(
                  value: CloudDestination.drive,
                  label: Text('Google Drive'),
                ),
              ],
              selected: {destination},
              onSelectionChanged: busy
                  ? null
                  : (v) => setState(() => destination = v.first),
            ),
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: busy || cloud.email == null
                  ? null
                  : () => run(() async {
                      validatePassword();
                      final bytes = await ref
                          .read(backupServiceProvider)
                          .create(password.text);
                      await cloud.upload(bytes, destination);
                      return 'Encrypted backup uploaded.';
                    }),
              icon: const Icon(Icons.cloud_upload_outlined),
              label: const Text('Back up to cloud'),
            ),
            const SizedBox(height: 12),
            OutlinedButton.icon(
              onPressed: busy || cloud.email == null
                  ? null
                  : () async {
                      if (!await confirmRestore()) return;
                      await run(() async {
                        final bytes = await cloud.download(destination);
                        await ref
                            .read(backupServiceProvider)
                            .restore(bytes, password.text);
                        refreshed();
                        return 'Cloud backup restored.';
                      });
                    },
              icon: const Icon(Icons.cloud_download_outlined),
              label: const Text('Restore from cloud'),
            ),
          ],
          if (busy)
            const Padding(
              padding: EdgeInsets.all(24),
              child: Center(child: CircularProgressIndicator()),
            ),
          if (message != null)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 20),
              child: Text(
                message!,
                style: TextStyle(color: Theme.of(context).colorScheme.primary),
              ),
            ),
          const SizedBox(height: 24),
          const Text(
            'Flutter backups use a new versioned format. Existing Compose database backup files cannot be restored directly.',
          ),
        ],
      ),
    );
  }
}
