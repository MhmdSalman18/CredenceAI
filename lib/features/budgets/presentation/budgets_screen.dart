import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../../core/theme/app_theme.dart';
import '../../../core/utils/money.dart';
import '../../../core/widgets/common.dart';
import '../../settings/application/settings_controller.dart';
import '../../transactions/application/transactions_controller.dart';
import '../../transactions/presentation/widgets/transaction_tile.dart';
import '../application/budgets_controller.dart';

class BudgetsScreen extends ConsumerWidget {
  const BudgetsScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) => Scaffold(
    appBar: AppBar(title: const Text('Smart Budget')),
    body: AsyncContent(
      value: ref.watch(budgetsProvider),
      retry: () => ref.invalidate(budgetsProvider),
      builder: (all) {
        final currency = ref.watch(settingsProvider).currency;
        final budgets = all.where((b) => b.currency == currency).toList();
        final transactions =
            ref.watch(transactionsProvider).asData?.value ?? [];
        final now = DateTime.now();
        return PageBody(
          children: [
            Text(
              'Give every rupee a purpose',
              style: Theme.of(context).textTheme.headlineSmall
                  ?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            const Text(
              'Set monthly limits, divide them across categories, and follow your progress.',
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: () => context.push('/budgets/new/edit'),
              icon: const Icon(Icons.add),
              label: const Text('Create a budget'),
            ),
            const SectionTitle('Your budgets'),
            if (budgets.isEmpty)
              const EmptyState(
                title: 'Plan your month',
                message:
                    'Create a budget to start tracking your spending limits.',
                icon: Icons.pie_chart_outline,
              )
            else
              ...budgets.map((budget) {
                final period = budget.appliesTo(now) ? now : budget.month;
                final spent = budget.spent(transactions, period);
                return Padding(
                  padding: const EdgeInsets.only(bottom: 14),
                  child: Card(
                    child: InkWell(
                      borderRadius: BorderRadius.circular(18),
                      onTap: () => context.push('/budgets/${budget.id}'),
                      child: Padding(
                        padding: const EdgeInsets.all(20),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Expanded(
                                  child: Text(
                                    budget.name,
                                    style: Theme.of(context)
                                        .textTheme
                                        .titleLarge,
                                  ),
                                ),
                                const Icon(Icons.chevron_right),
                              ],
                            ),
                            const SizedBox(height: 6),
                            Text(
                              '${DateFormat('MMM yyyy').format(period)}${budget.repeats ? ' · Repeats monthly' : ''}',
                            ),
                            const SizedBox(height: 18),
                            Text(
                              '${Money.format(spent, currency: currency)} of ${Money.format(budget.totalMinor, currency: currency)}',
                            ),
                            const SizedBox(height: 10),
                            LinearProgressIndicator(
                              value: (spent / budget.totalMinor).clamp(0, 1),
                              color: spent > budget.totalMinor
                                  ? BrandColors.red
                                  : BrandColors.cardBlue,
                              minHeight: 7,
                              borderRadius: BorderRadius.circular(8),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                );
              }),
          ],
        );
      },
    ),
  );
}

class BudgetDetailScreen extends ConsumerStatefulWidget {
  const BudgetDetailScreen({required this.id, super.key});
  final String id;
  @override
  ConsumerState<BudgetDetailScreen> createState() => _BudgetDetailScreenState();
}

class _BudgetDetailScreenState extends ConsumerState<BudgetDetailScreen> {
  DateTime? selectedMonth;
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      title: const Text('Budget details'),
      actions: [
        IconButton(
          tooltip: 'Edit budget',
          onPressed: () => context.push('/budgets/${widget.id}/edit'),
          icon: const Icon(Icons.edit_outlined),
        ),
      ],
    ),
    body: AsyncContent(
      value: ref.watch(budgetsProvider),
      retry: () => ref.invalidate(budgetsProvider),
      builder: (budgets) {
        final matches = budgets.where((b) => b.id == widget.id);
        if (matches.isEmpty) {
          return const Center(
            child: EmptyState(
              title: 'Budget not found',
              message: 'It may have been removed.',
            ),
          );
        }
        final budget = matches.first;
        final now = DateTime.now();
        final month =
            selectedMonth ??
            (budget.appliesTo(now)
                ? DateTime(now.year, now.month)
                : budget.month);
        return AsyncContent(
          value: ref.watch(transactionsProvider),
          retry: () => ref.invalidate(transactionsProvider),
          builder: (transactions) {
            final spent = budget.spent(transactions, month);
            final activity = transactions
                .where(
                  (t) =>
                      t.currency == budget.currency &&
                      t.isExpense &&
                      t.date.year == month.year &&
                      t.date.month == month.month,
                )
                .toList();
            String amount(int value) =>
                Money.format(value, currency: budget.currency);
            return PageBody(
              children: [
                Text(
                  budget.name,
                  style: Theme.of(context).textTheme.headlineMedium
                      ?.copyWith(fontWeight: FontWeight.bold),
                ),
                Row(
                  children: [
                    if (budget.repeats)
                      IconButton(
                        tooltip: 'Previous month',
                        onPressed:
                            !DateTime(month.year, month.month - 1).isBefore(
                              DateTime(budget.month.year, budget.month.month),
                            )
                            ? () => setState(
                                () => selectedMonth = DateTime(
                                  month.year,
                                  month.month - 1,
                                ),
                              )
                            : null,
                        icon: const Icon(Icons.chevron_left),
                      ),
                    Expanded(
                      child: Text(DateFormat('MMMM yyyy').format(month)),
                    ),
                    if (budget.repeats)
                      IconButton(
                        tooltip: 'Next month',
                        onPressed: () => setState(
                          () => selectedMonth = DateTime(
                            month.year,
                            month.month + 1,
                          ),
                        ),
                        icon: const Icon(Icons.chevron_right),
                      ),
                  ],
                ),
                const SizedBox(height: 16),
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Remaining budget'),
                        const SizedBox(height: 8),
                        Text(
                          amount(budget.totalMinor - spent),
                          style: Theme.of(context).textTheme.headlineMedium
                              ?.copyWith(
                                color: spent > budget.totalMinor
                                    ? BrandColors.red
                                    : BrandColors.green,
                                fontWeight: FontWeight.bold,
                              ),
                        ),
                        const SizedBox(height: 12),
                        Text(
                          '${amount(spent)} spent of ${amount(budget.totalMinor)}',
                        ),
                        const SizedBox(height: 14),
                        LinearProgressIndicator(
                          value: (spent / budget.totalMinor).clamp(0, 1),
                          minHeight: 7,
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ],
                    ),
                  ),
                ),
                const SectionTitle('Category allocations'),
                for (final entry in budget.allocations.entries)
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Builder(
                        builder: (context) {
                          final categorySpent = budget.spent(
                            transactions,
                            month,
                            category: entry.key,
                          );
                          return Column(
                            children: [
                              Row(
                                children: [
                                  Expanded(child: Text(entry.key)),
                                  Text(
                                    '${amount(categorySpent)} / ${amount(entry.value)}',
                                  ),
                                ],
                              ),
                              const SizedBox(height: 10),
                              LinearProgressIndicator(
                                value: entry.value == 0
                                    ? (categorySpent > 0 ? 1 : 0)
                                    : (categorySpent / entry.value).clamp(0, 1),
                                color: categorySpent > entry.value
                                    ? BrandColors.red
                                    : BrandColors.cardBlue,
                                minHeight: 5,
                              ),
                              const SizedBox(height: 8),
                            ],
                          );
                        },
                      ),
                    ),
                  ),
                const SectionTitle('This month’s expenses'),
                if (activity.isEmpty)
                  const EmptyState(
                    title: 'No expenses yet',
                    message: 'Expenses in this month will appear here.',
                  )
                else
                  ...activity.map(TransactionTile.new),
                const SizedBox(height: 20),
                TextButton.icon(
                  onPressed: () async {
                    if (!await confirmAction(
                      context,
                      title: 'Delete budget?',
                      message: 'Your transactions will stay in your records.',
                    )) {
                      return;
                    }
                    try {
                      await ref
                          .read(budgetsProvider.notifier)
                          .delete(widget.id);
                      if (context.mounted) context.pop();
                    } catch (e) {
                      if (context.mounted) showError(context, e);
                    }
                  },
                  icon: const Icon(Icons.delete_outline),
                  label: const Text('Delete budget'),
                ),
              ],
            );
          },
        );
      },
    ),
  );
}
