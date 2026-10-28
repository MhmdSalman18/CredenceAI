import '../../transactions/domain/transaction.dart';

class Budget {
  const Budget({
    required this.id,
    required this.name,
    required this.totalMinor,
    required this.month,
    required this.allocations,
    this.currency = 'INR',
    this.repeats = true,
  });
  final String id;
  final String name;
  final int totalMinor;
  final DateTime month;
  final Map<String, int> allocations;
  final String currency;
  final bool repeats;

  bool appliesTo(DateTime date) => repeats
      ? !DateTime(
          date.year,
          date.month,
        ).isBefore(DateTime(month.year, month.month))
      : date.year == month.year && date.month == month.month;

  int spent(
    List<FinanceTransaction> transactions,
    DateTime period, {
    String? category,
  }) => transactions
      .where(
        (t) =>
            t.isExpense &&
            t.currency == currency &&
            t.date.year == period.year &&
            t.date.month == period.month &&
            (category == null || t.category == category),
      )
      .fold(0, (sum, t) => sum + t.amountMinor);

  static Map<String, int> distribute(int total, List<String> categories) {
    if (categories.isEmpty) return {};
    final base = total ~/ categories.length;
    final remainder = total % categories.length;
    return {
      for (var i = 0; i < categories.length; i++)
        categories[i]: base + (i < remainder ? 1 : 0),
    };
  }
}
