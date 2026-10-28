import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:local_auth/local_auth.dart';

import '../features/notifications/data/notification_bridge.dart';
import '../features/settings/application/settings_controller.dart';
import '../features/settings/domain/app_settings.dart';

class AppServices extends ConsumerStatefulWidget {
  const AppServices({required this.child, super.key});
  final Widget child;
  @override
  ConsumerState<AppServices> createState() => _AppServicesState();
}

class _AppServicesState extends ConsumerState<AppServices>
    with WidgetsBindingObserver {
  StreamSubscription<dynamic>? subscription;
  ProviderSubscription<AppSettings>? settingsSubscription;
  bool locked = false;
  bool authenticating = false;
  bool importFailed = false;
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    locked = ref.read(settingsProvider).appLock;
    final bridge = ref.read(notificationBridgeProvider);
    subscription = bridge.events.listen(
      (_) => drain(),
      onError: (Object e) {
        if (mounted) setState(() => importFailed = true);
      },
    );
    settingsSubscription = ref.listenManual(settingsProvider, (previous, next) {
      unawaited(
        bridge.setTracking(next.tracking).catchError((Object e) {
          if (mounted) setState(() => importFailed = true);
        }),
      );
      if (!next.appLock && locked) setState(() => locked = false);
    });
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      try {
        await bridge.setTracking(ref.read(settingsProvider).tracking);
        await drain();
      } catch (_) {
        if (mounted) setState(() => importFailed = true);
      }
      if (locked && mounted) await unlock();
    });
  }

  Future<void> drain() async {
    try {
      await ref.read(notificationBridgeProvider).drain();
      if (mounted && importFailed) setState(() => importFailed = false);
    } catch (_) {
      if (mounted) setState(() => importFailed = true);
    }
  }

  Future<void> unlock() async {
    if (authenticating) return;
    setState(() => authenticating = true);
    try {
      final success = await LocalAuthentication().authenticate(
        localizedReason: 'Unlock Credence AI',
        persistAcrossBackgrounding: true,
      );
      if (success && mounted) setState(() => locked = false);
    } catch (_) {
      /* Keep the gate locked and allow a retry. */
    } finally {
      if (mounted) setState(() => authenticating = false);
    }
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.paused &&
        ref.read(settingsProvider).appLock &&
        !authenticating) {
      setState(() => locked = true);
    }
    if (state == AppLifecycleState.resumed) {
      unawaited(drain());
      if (locked && !authenticating) unawaited(unlock());
    }
  }

  @override
  void dispose() {
    subscription?.cancel();
    settingsSubscription?.close();
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Stack(
    children: [
      Offstage(offstage: locked, child: widget.child),
      if (locked)
        Positioned.fill(
          child: Material(
            color: Theme.of(context).scaffoldBackgroundColor,
            child: Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.lock_outline, size: 56),
                  const SizedBox(height: 20),
                  Text(
                    'Credence AI is locked',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 20),
                  FilledButton(
                    onPressed: authenticating ? null : unlock,
                    child: Text(authenticating ? 'Authenticating…' : 'Unlock'),
                  ),
                ],
              ),
            ),
          ),
        ),
      if (importFailed && !locked)
        Positioned(
          left: 16,
          right: 16,
          bottom: 90,
          child: Material(
            borderRadius: BorderRadius.circular(14),
            color: Theme.of(context).colorScheme.errorContainer,
            child: ListTile(
              title: const Text('Notification import needs a retry'),
              trailing: TextButton(
                onPressed: drain,
                child: const Text('Retry'),
              ),
            ),
          ),
        ),
    ],
  );
}
