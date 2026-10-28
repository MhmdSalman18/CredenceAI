import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../storage/app_database.dart';
import '../../features/transactions/data/sqlite_transaction_repository.dart';
import '../../features/transactions/domain/transaction_repository.dart';
import '../../features/budgets/data/sqlite_budget_repository.dart';
import '../../features/budgets/domain/budget_repository.dart';

final databaseProvider = Provider<AppDatabase>(
  (ref) => throw StateError('Database must be initialized at bootstrap'),
);
final preferencesProvider = Provider<SharedPreferences>(
  (ref) => throw StateError('Preferences must be initialized at bootstrap'),
);
final transactionRepositoryProvider = Provider<TransactionRepository>(
  (ref) => SqliteTransactionRepository(ref.watch(databaseProvider)),
);
final budgetRepositoryProvider = Provider<BudgetRepository>(
  (ref) => SqliteBudgetRepository(ref.watch(databaseProvider)),
);
