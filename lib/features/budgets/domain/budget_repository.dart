import 'budget.dart';

abstract interface class BudgetRepository {
  Future<List<Budget>> getAll();
  Future<void> save(Budget budget);
  Future<void> delete(String id);
}
