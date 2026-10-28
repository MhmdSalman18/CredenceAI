import 'dart:convert';

import 'package:sqflite/sqflite.dart';

import '../../../core/errors/app_exception.dart';
import '../../../core/storage/app_database.dart';
import '../domain/budget.dart';
import '../domain/budget_repository.dart';

Map<String, Object?> budgetToMap(Budget budget) => {
  'id': budget.id,
  'name': budget.name,
  'total_minor': budget.totalMinor,
  'month_ms': budget.month.millisecondsSinceEpoch,
  'allocations': jsonEncode(budget.allocations),
  'currency': budget.currency,
  'repeats': budget.repeats ? 1 : 0,
};

Budget budgetFromMap(Map<String, Object?> map) => Budget(
  id: map['id']! as String,
  name: map['name']! as String,
  totalMinor: map['total_minor']! as int,
  month: DateTime.fromMillisecondsSinceEpoch(map['month_ms']! as int),
  allocations: Map<String, int>.from(
    jsonDecode(map['allocations']! as String) as Map,
  ),
  currency: map['currency']! as String,
  repeats: map['repeats'] == 1,
);

class SqliteBudgetRepository implements BudgetRepository {
  SqliteBudgetRepository(this.database);
  final AppDatabase database;
  @override
  Future<List<Budget>> getAll() async => (await database.connection.query(
    'budgets',
    orderBy: 'month_ms DESC',
  )).map(budgetFromMap).toList(growable: false);
  @override
  Future<void> save(Budget budget) async {
    if (budget.name.trim().isEmpty ||
        budget.totalMinor <= 0 ||
        budget.allocations.values.any((v) => v < 0) ||
        budget.allocations.values.fold(0, (a, b) => a + b) >
            budget.totalMinor) {
      throw const AppException(
        'Use a positive budget and keep allocations within its total.',
      );
    }
    await database.connection.insert(
      'budgets',
      budgetToMap(budget),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  @override
  Future<void> delete(String id) async {
    await database.connection.delete(
      'budgets',
      where: 'id = ?',
      whereArgs: [id],
    );
  }
}
