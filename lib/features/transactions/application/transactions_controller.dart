import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/di/dependencies.dart';
import '../domain/transaction.dart';

final transactionsProvider =
    AsyncNotifierProvider<TransactionsController, List<FinanceTransaction>>(
      TransactionsController.new,
    );

class TransactionsController extends AsyncNotifier<List<FinanceTransaction>> {
  @override
  Future<List<FinanceTransaction>> build() =>
      ref.watch(transactionRepositoryProvider).getAll();

  Future<void> save(FinanceTransaction value) async {
    await ref.read(transactionRepositoryProvider).save(value);
    ref.invalidateSelf();
    await future;
  }

  Future<void> delete(String id) async {
    await ref.read(transactionRepositoryProvider).delete(id);
    ref.invalidateSelf();
    await future;
  }

  Future<void> clear() async {
    await ref.read(transactionRepositoryProvider).clear();
    ref.invalidateSelf();
    await future;
  }
}
