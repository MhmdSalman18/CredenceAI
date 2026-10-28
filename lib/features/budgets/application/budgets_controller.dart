import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/di/dependencies.dart';
import '../domain/budget.dart';

final budgetsProvider = AsyncNotifierProvider<BudgetsController, List<Budget>>(
  BudgetsController.new,
);

class BudgetsController extends AsyncNotifier<List<Budget>> {
  @override
  Future<List<Budget>> build() => ref.watch(budgetRepositoryProvider).getAll();
  Future<void> save(Budget budget) async {
    await ref.read(budgetRepositoryProvider).save(budget);
    ref.invalidateSelf();
    await future;
  }

  Future<void> delete(String id) async {
    await ref.read(budgetRepositoryProvider).delete(id);
    ref.invalidateSelf();
    await future;
  }
}
