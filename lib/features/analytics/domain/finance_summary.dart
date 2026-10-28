import '../../transactions/domain/transaction.dart';

class FinanceSummary {
  FinanceSummary(
    Iterable<FinanceTransaction> all,
    DateTime month,
    String currency,
  ) {
    for (final transaction in all) {
      if (transaction.currency != currency ||
          transaction.date.year != month.year ||
          transaction.date.month != month.month) {
        continue;
      }
      if (transaction.isExpense) {
        expenseMinor += transaction.amountMinor;
        final category = transaction.category ?? 'Uncategorized';
        categories.update(
          category,
          (value) => value + transaction.amountMinor,
          ifAbsent: () => transaction.amountMinor,
        );
        dailyExpenses.update(
          transaction.date.day,
          (value) => value + transaction.amountMinor,
          ifAbsent: () => transaction.amountMinor,
        );
      } else {
        incomeMinor += transaction.amountMinor;
      }
      count++;
    }
  }
  int incomeMinor = 0;
  int expenseMinor = 0;
  int count = 0;
  final Map<String, int> categories = {};
  final Map<int, int> dailyExpenses = {};
  int get balanceMinor => incomeMinor - expenseMinor;
  List<MapEntry<String, int>> get rankedCategories =>
      categories.entries.toList()..sort((a, b) => b.value.compareTo(a.value));
}
