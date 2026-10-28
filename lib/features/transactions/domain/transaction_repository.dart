import 'transaction.dart';

abstract interface class TransactionRepository {
  Future<List<FinanceTransaction>> getAll();
  Future<void> save(FinanceTransaction transaction);
  Future<void> delete(String id);
  Future<void> clear();
}
