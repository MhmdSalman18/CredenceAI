import 'package:sqflite/sqflite.dart';

import '../../../core/errors/app_exception.dart';
import '../../../core/storage/app_database.dart';
import '../domain/transaction.dart';
import '../domain/transaction_repository.dart';

class SqliteTransactionRepository implements TransactionRepository {
  SqliteTransactionRepository(this.database);
  final AppDatabase database;

  @override
  Future<List<FinanceTransaction>> getAll() async =>
      (await database.connection.query(
        'transactions',
        orderBy: 'date_ms DESC',
      )).map(FinanceTransaction.fromMap).toList(growable: false);

  @override
  Future<void> save(FinanceTransaction transaction) async {
    if (transaction.amountMinor <= 0 ||
        transaction.merchant.trim().isEmpty ||
        transaction.id.isEmpty) {
      throw const AppException(
        'Enter a positive amount and a merchant or income source.',
      );
    }
    await database.connection.transaction((db) async {
      // Notification references are idempotent; editing still updates by ID.
      final existing = await db.query(
        'transactions',
        columns: ['id'],
        where: 'id = ?',
        whereArgs: [transaction.id],
      );
      if (existing.isNotEmpty) {
        await db.update(
          'transactions',
          transaction.toMap(),
          where: 'id = ?',
          whereArgs: [transaction.id],
        );
      } else {
        await db.insert(
          'transactions',
          transaction.toMap(),
          conflictAlgorithm: ConflictAlgorithm.ignore,
        );
      }
    });
  }

  @override
  Future<void> delete(String id) async {
    await database.connection.delete(
      'transactions',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  @override
  Future<void> clear() async {
    await database.connection.delete('transactions');
  }
}
