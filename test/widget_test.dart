import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:credence_ai_flutter/app/credence_app.dart';
import 'package:credence_ai_flutter/core/di/dependencies.dart';
import 'package:credence_ai_flutter/features/transactions/domain/transaction.dart';
import 'package:credence_ai_flutter/features/transactions/domain/transaction_repository.dart';
import 'package:credence_ai_flutter/features/budgets/domain/budget.dart';
import 'package:credence_ai_flutter/features/budgets/domain/budget_repository.dart';

class _Transactions implements TransactionRepository {
  final rows = <FinanceTransaction>[];
  @override
  Future<List<FinanceTransaction>> getAll() async => rows;
  @override
  Future<void> save(FinanceTransaction transaction) async {
    rows.removeWhere((t) => t.id == transaction.id);
    rows.add(transaction);
  }

  @override
  Future<void> delete(String id) async => rows.removeWhere((t) => t.id == id);
  @override
  Future<void> clear() async => rows.clear();
}

class _Budgets implements BudgetRepository {
  @override
  Future<List<Budget>> getAll() async => [];
  @override
  Future<void> save(Budget budget) async {}
  @override
  Future<void> delete(String id) async {}
}

void main() {
  testWidgets('dashboard, validation and navigation render at phone width', (
    tester,
  ) async {
    SharedPreferences.setMockInitialValues({});
    final preferences = await SharedPreferences.getInstance();
    tester.view.physicalSize = const Size(390, 844);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          transactionRepositoryProvider.overrideWithValue(_Transactions()),
          budgetRepositoryProvider.overrideWithValue(_Budgets()),
          preferencesProvider.overrideWithValue(preferences),
        ],
        child: const CredenceApp(),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.text('Hello, New User!'), findsOneWidget);
    await tester.scrollUntilVisible(
      find.text('Your finances start here'),
      250,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Your finances start here'), findsOneWidget);
    await tester.scrollUntilVisible(
      find.text('Add expense').first,
      -250,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.tap(find.text('Add expense').first);
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(
      find.text('Save transaction'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.tap(find.text('Save transaction'));
    await tester.pumpAndSettle();
    expect(find.text('Enter a name'), findsOneWidget);
    await tester.ensureVisible(find.byType(TextFormField).at(0));
    await tester.enterText(find.byType(TextFormField).at(0), '125.50');
    await tester.ensureVisible(find.byType(TextFormField).at(1));
    await tester.enterText(find.byType(TextFormField).at(1), 'Coffee Corner');
    await tester.ensureVisible(
      find.byType(DropdownButtonFormField<String>).first,
    );
    await tester.tap(find.byType(DropdownButtonFormField<String>).first);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Food & Dining').last);
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.text('Save transaction'));
    await tester.tap(find.text('Save transaction'));
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(
      find.text('Coffee Corner'),
      250,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Coffee Corner'), findsOneWidget);
    await tester.tap(find.text('Analytics').last);
    await tester.pumpAndSettle();
    expect(find.text('Spending by category'), findsOneWidget);
    // Layout exceptions are reported by the test binding.
    await tester.pumpWidget(const SizedBox.shrink());
  });
}
