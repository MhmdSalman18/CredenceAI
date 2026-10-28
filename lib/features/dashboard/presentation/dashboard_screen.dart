import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../../core/theme/app_theme.dart';
import '../../../core/utils/money.dart';
import '../../../core/widgets/common.dart';
import '../../analytics/domain/finance_summary.dart';
import '../../backup/application/export_service.dart';
import '../../budgets/application/budgets_controller.dart';
import '../../settings/application/settings_controller.dart';
import '../../transactions/application/transactions_controller.dart';
import '../../transactions/presentation/widgets/transaction_tile.dart';

class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settings = ref.watch(settingsProvider);
    final now = DateTime.now();
    return AsyncContent(
      value: ref.watch(transactionsProvider),
      retry: () => ref.invalidate(transactionsProvider),
      builder: (transactions) {
        final summary = FinanceSummary(transactions, now, settings.currency);
        final budgets = (ref.watch(budgetsProvider).asData?.value ?? []).where(
          (b) => b.currency == settings.currency && b.appliesTo(now),
        );
        final budget = budgets.fold(0, (sum, b) => sum + b.totalMinor);
        final uncategorized = transactions
            .where((t) => t.isUncategorized)
            .length;
        String amount(int value) => settings.hideBalance
            ? '••••••'
            : Money.format(value, currency: settings.currency);
        return RefreshIndicator(
          onRefresh: () async {
            ref.invalidate(budgetsProvider);
            ref.invalidate(transactionsProvider);
            await ref.read(transactionsProvider.future);
          },
          child: PageBody(
            children: [
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Hello, ${settings.userName}!',
                          style: Theme.of(context).textTheme.headlineSmall
                              ?.copyWith(fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 4),
                        const Text('Welcome back to your finances'),
                      ],
                    ),
                  ),
                  IconButton(
                    tooltip: 'Notifications',
                    onPressed: () => context.push('/notifications'),
                    icon: const Icon(Icons.notifications_outlined),
                  ),
                ],
              ),
              const SizedBox(height: 24),
              Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(24),
                  gradient: const LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [BrandColors.cardBlue, BrandColors.cardBlueEnd],
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            DateFormat('MMMM yyyy').format(now),
                            style: const TextStyle(color: Colors.white70),
                          ),
                        ),
                        IconButton(
                          tooltip: settings.hideBalance
                              ? 'Show balance'
                              : 'Hide balance',
                          onPressed: () async {
                            try {
                              await ref
                                  .read(settingsProvider.notifier)
                                  .update(
                                    settings.copyWith(
                                      hideBalance: !settings.hideBalance,
                                    ),
                                  );
                            } catch (e) {
                              if (context.mounted) showError(context, e);
                            }
                          },
                          icon: Icon(
                            settings.hideBalance
                                ? Icons.visibility_off_outlined
                                : Icons.visibility_outlined,
                            color: Colors.white70,
                          ),
                        ),
                      ],
                    ),
                    const Text(
                      'Available balance',
                      style: TextStyle(color: Colors.white70),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      amount(summary.balanceMinor),
                      style: const TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                    const SizedBox(height: 24),
                    Row(
                      children: [
                        Expanded(
                          child: _SummaryMetric(
                            label: 'Income',
                            amount: amount(summary.incomeMinor),
                            icon: Icons.north_east,
                            color: const Color(0xFF7AE6A3),
                          ),
                        ),
                        Expanded(
                          child: _SummaryMetric(
                            label: 'Spent',
                            amount: amount(summary.expenseMinor),
                            icon: Icons.south_west,
                            color: const Color(0xFFFFB2B2),
                          ),
                        ),
                      ],
                    ),
                    if (budget > 0) ...[
                      const SizedBox(height: 24),
                      LinearProgressIndicator(
                        value: (summary.expenseMinor / budget).clamp(0, 1),
                        backgroundColor: Colors.white24,
                        color: summary.expenseMinor > budget
                            ? const Color(0xFFFFB2B2)
                            : const Color(0xFF7AE6A3),
                        borderRadius: BorderRadius.circular(8),
                        minHeight: 6,
                      ),
                      const SizedBox(height: 10),
                      Text(
                        '${amount((budget - summary.expenseMinor).clamp(0, budget))} left of ${amount(budget)} budget',
                        style: const TextStyle(color: Colors.white70),
                      ),
                    ],
                  ],
                ),
              ),
              if (uncategorized > 0) ...[
                const SizedBox(height: 16),
                Card(
                  child: ListTile(
                    leading: const Icon(Icons.category_outlined),
                    title: const Text('Uncategorized activity'),
                    subtitle: Text('$uncategorized transactions to review'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => context.push('/uncategorized'),
                  ),
                ),
              ],
              const SectionTitle('Quick actions'),
              LayoutBuilder(
                builder: (context, constraints) => Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: [
                    _QuickAction(
                      icon: Icons.add,
                      label: 'Add expense',
                      onTap: () => context.push('/transaction/new'),
                    ),
                    _QuickAction(
                      icon: Icons.pie_chart_outline,
                      label: 'Smart budget',
                      onTap: () => context.push('/budgets'),
                    ),
                    _QuickAction(
                      icon: Icons.file_download_outlined,
                      label: 'Export report',
                      onTap: () async {
                        try {
                          await ref
                              .read(exportServiceProvider)
                              .share(transactions);
                        } catch (e) {
                          if (context.mounted) showError(context, e);
                        }
                      },
                    ),
                  ],
                ),
              ),
              SectionTitle(
                'Recent transactions',
                action: TextButton(
                  onPressed: () => context.go('/history'),
                  child: const Text('See all'),
                ),
              ),
              if (transactions.isEmpty)
                EmptyState(
                  title: 'Your finances start here',
                  message: 'Add your first transaction or enable notification tracking in Settings.',
                  action: FilledButton.icon(
                    onPressed: () => context.push('/transaction/new'),
                    icon: const Icon(Icons.add),
                    label: const Text('Add transaction'),
                  ),
                )
              else
                ...transactions.take(5).map(TransactionTile.new),
              SectionTitle(
                'Top categories',
                action: TextButton(
                  onPressed: () => context.go('/analytics'),
                  child: const Text('View analytics'),
                ),
              ),
              if (summary.categories.isEmpty)
                const EmptyState(
                  title: 'No spending this month',
                  message: 'Your spending categories will appear here.',
                  icon: Icons.donut_large,
                )
              else
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      children: summary.rankedCategories
                          .take(4)
                          .map(
                            (entry) => Padding(
                              padding: const EdgeInsets.symmetric(vertical: 8),
                              child: Row(
                                children: [
                                  const Icon(
                                    Icons.circle,
                                    size: 10,
                                    color: BrandColors.cardBlue,
                                  ),
                                  const SizedBox(width: 12),
                                  Expanded(child: Text(entry.key)),
                                  Text(
                                    amount(entry.value),
                                    style: const TextStyle(
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          )
                          .toList(),
                    ),
                  ),
                ),
            ],
          ),
        );
      },
    );
  }
}

class _SummaryMetric extends StatelessWidget {
  const _SummaryMetric({
    required this.label,
    required this.amount,
    required this.icon,
    required this.color,
  });
  final String label;
  final String amount;
  final IconData icon;
  final Color color;
  @override
  Widget build(BuildContext context) => Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Row(
        children: [
          Icon(icon, size: 16, color: color),
          const SizedBox(width: 6),
          Text(label, style: const TextStyle(color: Colors.white70)),
        ],
      ),
      const SizedBox(height: 6),
      Text(
        amount,
        style: const TextStyle(
          color: Colors.white,
          fontWeight: FontWeight.bold,
          fontSize: 18,
        ),
      ),
    ],
  );
}

class _QuickAction extends StatelessWidget {
  const _QuickAction({
    required this.icon,
    required this.label,
    required this.onTap,
  });
  final IconData icon;
  final String label;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) => OutlinedButton.icon(
    onPressed: onTap,
    icon: Icon(icon, size: 20),
    label: Text(label),
    style: OutlinedButton.styleFrom(
      padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 14),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
    ),
  );
}
