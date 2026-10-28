import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/di/dependencies.dart';
import '../../../core/errors/app_exception.dart';
import '../../../core/storage/app_database.dart';
import '../../budgets/data/sqlite_budget_repository.dart';
import '../../transactions/domain/transaction.dart';
import '../data/backup_cipher.dart';

final backupServiceProvider = Provider(
  (ref) => BackupService(ref.watch(databaseProvider)),
);

class BackupService {
  BackupService(this.database, {BackupCipher? cipher})
    : cipher = cipher ?? BackupCipher();
  final AppDatabase database;
  final BackupCipher cipher;

  Future<Uint8List> create(String password) async {
    final document = await database.connection.transaction(
      (db) async => {
        'schema_version': 1,
        'created_at': DateTime.now().toUtc().toIso8601String(),
        'transactions': await db.query('transactions'),
        'budgets': await db.query('budgets'),
      },
    );
    final json = jsonEncode(document);
    if (utf8.encode(json).length > BackupCipher.maxBytes ~/ 2) {
      throw const AppException(
        'The backup exceeds the supported size. Export older transactions first.',
      );
    }
    return cipher.encrypt(json, password);
  }

  /// Decode and validate the entire document before changing any existing data.
  Future<void> restore(Uint8List bytes, String password) async {
    final text = await cipher.decrypt(bytes, password);
    late List<FinanceTransaction> transactions;
    late List<Map<String, Object?>> budgets;
    try {
      final decoded = jsonDecode(text) as Map<String, dynamic>;
      if (decoded['schema_version'] != 1) throw const FormatException();
      transactions = (decoded['transactions'] as List)
          .map(
            (row) => FinanceTransaction.fromMap(
              Map<String, Object?>.from(row as Map),
            ),
          )
          .toList();
      final ids = <String>{};
      final references = <String>{};
      for (final t in transactions) {
        if (t.amountMinor <= 0 ||
            t.merchant.trim().isEmpty ||
            t.id.isEmpty ||
            !ids.add(t.id) ||
            (t.referenceId != null && !references.add(t.referenceId!))) {
          throw const FormatException();
        }
      }
      final budgetIds = <String>{};
      budgets = (decoded['budgets'] as List).map((row) {
        final budget = budgetFromMap(Map<String, Object?>.from(row as Map));
        if (budget.id.isEmpty ||
            !budgetIds.add(budget.id) ||
            budget.name.trim().isEmpty ||
            budget.totalMinor <= 0 ||
            budget.allocations.values.any((v) => v < 0) ||
            budget.allocations.values.fold(0, (a, b) => a + b) >
                budget.totalMinor) {
          throw const FormatException();
        }
        return budgetToMap(budget);
      }).toList();
    } catch (_) {
      throw const AppException(
        'This backup contains invalid or unsupported data. Your records were not changed.',
      );
    }
    await database.connection.transaction((db) async {
      await db.delete('transactions');
      await db.delete('budgets');
      for (final t in transactions) {
        await db.insert('transactions', t.toMap());
      }
      for (final b in budgets) {
        await db.insert('budgets', b);
      }
    });
  }
}
