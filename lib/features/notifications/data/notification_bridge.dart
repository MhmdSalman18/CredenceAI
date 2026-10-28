import 'dart:io';

import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/di/dependencies.dart';
import '../../transactions/application/transactions_controller.dart';
import '../../transactions/domain/transaction.dart';

final notificationBridgeProvider = Provider((ref) => NotificationBridge(ref));

class NotificationBridge {
  NotificationBridge(this.ref);
  final Ref ref;
  static const _methods = MethodChannel('com.credenceai/notifications');
  static const _events = EventChannel('com.credenceai/notification_events');
  bool _draining = false;
  bool _requested = false;
  bool get supported => Platform.isAndroid;
  Stream<dynamic> get events =>
      supported ? _events.receiveBroadcastStream() : const Stream.empty();

  Future<bool> hasAccess() async =>
      supported && (await _methods.invokeMethod<bool>('hasAccess') ?? false);
  Future<void> openSettings() async {
    if (supported) await _methods.invokeMethod<void>('openSettings');
  }

  Future<void> setTracking(bool enabled) async {
    if (supported) await _methods.invokeMethod<void>('setTracking', enabled);
  }

  /// Persist first, acknowledge second. A crash between them is safe because
  /// notification reference IDs are unique in the transaction database.
  Future<void> drain() async {
    if (!supported) return;
    _requested = true;
    if (_draining) return;
    _draining = true;
    var changed = false;
    try {
      while (_requested) {
        _requested = false;
        final events =
            await _methods.invokeListMethod<dynamic>('pending') ?? [];
        for (final event in events) {
          final map = Map<String, Object?>.from(event as Map);
          final transaction = FinanceTransaction.fromMap(map);
          await ref.read(transactionRepositoryProvider).save(transaction);
          await _methods.invokeMethod<void>('acknowledge', transaction.id);
          changed = true;
        }
        if (events.length >= 200) _requested = true;
      }
    } finally {
      _draining = false;
      if (changed) ref.invalidate(transactionsProvider);
    }
  }
}
