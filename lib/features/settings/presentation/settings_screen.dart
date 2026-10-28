import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:local_auth/local_auth.dart';

import '../../../core/errors/app_exception.dart';
import '../../../core/widgets/common.dart';
import '../../notifications/data/notification_bridge.dart';
import '../../transactions/application/transactions_controller.dart';
import '../application/settings_controller.dart';
import '../domain/app_settings.dart';

class SettingsScreen extends ConsumerStatefulWidget {
  const SettingsScreen({super.key});
  @override
  ConsumerState<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends ConsumerState<SettingsScreen> {
  bool saving = false;
  Future<void> update(AppSettings value) async {
    setState(() => saving = true);
    try {
      await ref.read(notificationBridgeProvider).setTracking(value.tracking);
      await ref.read(settingsProvider.notifier).update(value);
    } catch (e) {
      if (mounted) showError(context, e);
    } finally {
      if (mounted) setState(() => saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final settings = ref.watch(settingsProvider);
    return PageBody(
      children: [
        Card(
          child: ListTile(
            leading: const CircleAvatar(child: Icon(Icons.person_outline)),
            title: Text(settings.userName),
            subtitle: const Text('Personal profile'),
            trailing: const Icon(Icons.edit_outlined),
            onTap: saving
                ? null
                : () async {
                    final controller = TextEditingController(
                      text: settings.userName,
                    );
                    final value = await showDialog<String>(
                      context: context,
                      builder: (context) => AlertDialog(
                        title: const Text('Your name'),
                        content: TextField(
                          controller: controller,
                          maxLength: 40,
                          autofocus: true,
                          decoration: const InputDecoration(labelText: 'Name'),
                        ),
                        actions: [
                          TextButton(
                            onPressed: () => Navigator.pop(context),
                            child: const Text('Cancel'),
                          ),
                          FilledButton(
                            onPressed: () {
                              if (controller.text.trim().isNotEmpty) {
                                Navigator.pop(context, controller.text.trim());
                              }
                            },
                            child: const Text('Save'),
                          ),
                        ],
                      ),
                    );
                    // The dialog route may still animate after its result completes.
                    if (value != null && mounted) {
                      await update(settings.copyWith(userName: value));
                    }
                    Future<void>.delayed(
                      const Duration(milliseconds: 400),
                      controller.dispose,
                    );
                  },
          ),
        ),
        const SectionTitle('Preferences'),
        Card(
          child: Column(
            children: [
              SwitchListTile(
                title: const Text('Dark mode'),
                secondary: const Icon(Icons.dark_mode_outlined),
                value: settings.darkMode,
                onChanged: saving
                    ? null
                    : (v) => update(settings.copyWith(darkMode: v)),
              ),
              SwitchListTile(
                title: const Text('Hide dashboard balance'),
                secondary: const Icon(Icons.visibility_off_outlined),
                value: settings.hideBalance,
                onChanged: saving
                    ? null
                    : (v) => update(settings.copyWith(hideBalance: v)),
              ),
              ListTile(
                leading: const Icon(Icons.currency_rupee),
                title: const Text('Currency'),
                subtitle: const Text(
                  'Totals are separated by currency; no conversion is applied.',
                ),
                trailing: DropdownButton<String>(
                  value: settings.currency,
                  underline: const SizedBox(),
                  items: ['INR', 'USD', 'EUR', 'GBP']
                      .map((c) => DropdownMenuItem(value: c, child: Text(c)))
                      .toList(),
                  onChanged: saving
                      ? null
                      : (v) {
                          if (v != null) update(settings.copyWith(currency: v));
                        },
                ),
              ),
            ],
          ),
        ),
        const SectionTitle('Privacy & tracking'),
        Card(
          child: Column(
            children: [
              SwitchListTile(
                title: const Text('Automatic transaction tracking'),
                subtitle: Text(
                  Platform.isAndroid
                      ? 'Reads payment and bank notifications after you grant access.'
                      : 'Available on Android phones.',
                ),
                secondary: const Icon(Icons.auto_awesome_outlined),
                value: settings.tracking,
                onChanged: saving || !Platform.isAndroid
                    ? null
                    : (v) => update(settings.copyWith(tracking: v)),
              ),
              if (Platform.isAndroid)
                ListTile(
                  leading: const Icon(Icons.notifications_active_outlined),
                  title: const Text('Notification access'),
                  subtitle: const Text(
                    'Grant Android permission to capture new transactions',
                  ),
                  trailing: const Icon(Icons.open_in_new),
                  onTap: () async {
                    if (!await confirmAction(
                      context,
                      title: 'Allow transaction tracking?',
                      message: 'Credence AI reads notifications from supported payment, banking, and SMS apps. Parsed transaction details are stored locally and can be reviewed or deleted. Enable access for Credence AI Flutter on the next screen.',
                      action: 'Open settings',
                    )) {
                      return;
                    }
                    try {
                      await ref.read(notificationBridgeProvider).openSettings();
                    } catch (e) {
                      if (context.mounted) showError(context, e);
                    }
                  },
                ),
              SwitchListTile(
                title: const Text('App lock'),
                subtitle: const Text(
                  'Use your device fingerprint, PIN, or password',
                ),
                secondary: const Icon(Icons.lock_outline),
                value: settings.appLock,
                onChanged: saving
                    ? null
                    : (v) async {
                        try {
                          final auth = LocalAuthentication();
                          if (!await auth.isDeviceSupported()) {
                            throw const AppException(
                              'Set up a device screen lock before enabling app lock.',
                            );
                          }
                          if (await auth.authenticate(
                            localizedReason:
                                'Confirm the Credence AI app lock setting',
                          )) {
                            await update(settings.copyWith(appLock: v));
                          }
                        } catch (e) {
                          if (context.mounted) showError(context, e);
                        }
                      },
              ),
            ],
          ),
        ),
        const SectionTitle('Your data'),
        Card(
          child: Column(
            children: [
              ListTile(
                leading: const Icon(Icons.backup_outlined),
                title: const Text('Backup & restore'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => context.push('/backup'),
              ),
              ListTile(
                leading: const Icon(Icons.category_outlined),
                title: const Text('Uncategorized transactions'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => context.push('/uncategorized'),
              ),
              ListTile(
                leading: const Icon(Icons.account_balance_outlined),
                title: const Text('Supported banks & apps'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => context.push('/supported-banks'),
              ),
              ListTile(
                leading: Icon(
                  Icons.delete_outline,
                  color: Theme.of(context).colorScheme.error,
                ),
                title: const Text('Clear all transactions'),
                onTap: saving
                    ? null
                    : () async {
                        if (!await confirmAction(
                          context,
                          title: 'Clear all transactions?',
                          message: 'This permanently removes your local transaction history. Budgets remain. Save a backup first if needed.',
                          action: 'Clear transactions',
                        )) {
                          return;
                        }
                        try {
                          await ref.read(transactionsProvider.notifier).clear();
                          if (context.mounted) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text('Transactions cleared.'),
                              ),
                            );
                          }
                        } catch (e) {
                          if (context.mounted) showError(context, e);
                        }
                      },
              ),
            ],
          ),
        ),
        const SizedBox(height: 28),
        const Center(
          child: Text(
            'Credence AI Flutter · 1.0.0\nYour finances, in your control.',
            textAlign: TextAlign.center,
          ),
        ),
      ],
    );
  }
}
