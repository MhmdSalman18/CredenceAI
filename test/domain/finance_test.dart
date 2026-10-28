import 'package:flutter_test/flutter_test.dart';
import 'package:credence_ai_flutter/core/utils/money.dart';
import 'package:credence_ai_flutter/features/transactions/domain/transaction.dart';
import 'package:credence_ai_flutter/features/analytics/domain/finance_summary.dart';
import 'package:credence_ai_flutter/features/budgets/domain/budget.dart';
import 'package:credence_ai_flutter/features/backup/application/export_service.dart';

FinanceTransaction transaction(
  String id,
  int amount, {
  DateTime? date,
  String currency = 'INR',
  TransactionType type = TransactionType.debit,
  String merchant = 'Coffee',
  String? category = 'Food & Dining',
}) => FinanceTransaction(
  id: id,
  amountMinor: amount,
  type: type,
  merchant: merchant,
  date: date ?? DateTime(2026, 10, 12),
  currency: currency,
  category: category,
);

void main() {
  test(
    'decimal amounts retain exact minor units and reject invalid values',
    () {
      expect(Money.parse('0.10'), 10);
      expect(Money.parse('1,20,000.99'), 12000099);
      expect(Money.parse('12.3'), 1230);
      for (final invalid in [
        '-1',
        'NaN',
        '1.001',
        '1e3',
        '999999999999999999999999',
      ]) {
        expect(Money.parse(invalid), isNull);
      }
    },
  );

  test('monthly totals exclude adjacent months and other currencies', () {
    final summary = FinanceSummary(
      [
        transaction('a', 50000, type: TransactionType.credit),
        transaction('b', 12345),
        transaction('c', 8000, date: DateTime(2026, 9, 30, 23, 59)),
        transaction('d', 9000, date: DateTime(2026, 11, 1)),
        transaction('e', 999999, currency: 'USD'),
        transaction('f', 1, category: null),
      ],
      DateTime(2026, 10),
      'INR',
    );
    expect(summary.incomeMinor, 50000);
    expect(summary.expenseMinor, 12346);
    expect(summary.balanceMinor, 37654);
    expect(summary.categories['Uncategorized'], 1);
    expect(summary.count, 3);
  });

  test('allocation rounding conserves every minor unit', () {
    final allocation = Budget.distribute(10000, ['Food', 'Bills', 'Travel']);
    expect(allocation.values.reduce((a, b) => a + b), 10000);
    expect(allocation.values.toList(), [3334, 3333, 3333]);
    expect(Budget.distribute(100, []), isEmpty);
  });

  test('recurring budgets apply from their start month and spending respects currency', () {
    final budget = Budget(
      id: 'b',
      name: 'Plan',
      totalMinor: 10000,
      month: DateTime(2026, 10),
      allocations: const {},
    );
    expect(budget.appliesTo(DateTime(2026, 9)), isFalse);
    expect(budget.appliesTo(DateTime(2027, 1)), isTrue);
    expect(
      budget.spent([
        transaction('a', 25),
        transaction('b', 2000, currency: 'USD'),
        transaction('c', 1000, type: TransactionType.credit),
      ], DateTime(2026, 10)),
      25,
    );
  });

  test('CSV escapes quotes, newlines and spreadsheet formula injection', () {
    final csv = ExportService().csv([
      transaction('a', 100, merchant: '=HYPERLINK("bad")\nshop'),
    ]);
    expect(csv, contains('"\'=HYPERLINK(""bad"")\nshop"'));
    expect(csv, contains('"1.00"'));
  });
}
