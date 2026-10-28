import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../../core/theme/app_theme.dart';
import '../../../core/utils/money.dart';
import '../../../core/widgets/common.dart';
import '../../settings/application/settings_controller.dart';
import '../../transactions/application/transactions_controller.dart';
import '../domain/finance_summary.dart';

class AnalyticsScreen extends ConsumerStatefulWidget {
  const AnalyticsScreen({super.key});
  @override
  ConsumerState<AnalyticsScreen> createState() => _AnalyticsScreenState();
}

class _AnalyticsScreenState extends ConsumerState<AnalyticsScreen> {
  DateTime month = DateTime(DateTime.now().year, DateTime.now().month);
  @override
  Widget build(BuildContext context) {
    final currency = ref.watch(settingsProvider).currency;
    return AsyncContent(
      value: ref.watch(transactionsProvider),
      retry: () => ref.invalidate(transactionsProvider),
      builder: (transactions) {
        final summary = FinanceSummary(transactions, month, currency);
        String amount(int value) => Money.format(value, currency: currency);
        return PageBody(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                IconButton(
                  tooltip: 'Previous month',
                  onPressed: () => setState(
                    () => month = DateTime(month.year, month.month - 1),
                  ),
                  icon: const Icon(Icons.chevron_left),
                ),
                Expanded(
                  child: Text(
                    DateFormat('MMMM yyyy').format(month),
                    textAlign: TextAlign.center,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                ),
                IconButton(
                  tooltip: 'Next month',
                  onPressed: () => setState(
                    () => month = DateTime(month.year, month.month + 1),
                  ),
                  icon: const Icon(Icons.chevron_right),
                ),
              ],
            ),
            const SizedBox(height: 20),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _Metric(
                  label: 'Income',
                  value: amount(summary.incomeMinor),
                  color: BrandColors.green,
                ),
                _Metric(
                  label: 'Expenses',
                  value: amount(summary.expenseMinor),
                  color: BrandColors.red,
                ),
                _Metric(
                  label: 'Net savings',
                  value: amount(summary.balanceMinor),
                  color: BrandColors.cardBlue,
                ),
              ],
            ),
            const SectionTitle('Spending by category'),
            if (summary.expenseMinor == 0)
              const EmptyState(
                title: 'No spending to analyse',
                message: 'Add expenses or choose another month.',
                icon: Icons.insights_outlined,
              )
            else
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    children: [
                      Semantics(
                        label: 'Total spending ${amount(summary.expenseMinor)}',
                        child: SizedBox(
                          width: 200,
                          height: 200,
                          child: CustomPaint(
                            painter: _DonutPainter(
                              summary.rankedCategories
                                  .map((e) => e.value)
                                  .toList(),
                            ),
                            child: Center(
                              child: Column(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  const Text('Total spent'),
                                  const SizedBox(height: 6),
                                  Text(
                                    amount(summary.expenseMinor),
                                    style: const TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontSize: 19,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(height: 20),
                      for (var i = 0; i < summary.rankedCategories.length; i++)
                        Padding(
                          padding: const EdgeInsets.symmetric(vertical: 10),
                          child: Column(
                            children: [
                              Row(
                                children: [
                                  Icon(
                                    Icons.circle,
                                    color:
                                        BrandColors.categories[i %
                                            BrandColors.categories.length],
                                    size: 12,
                                  ),
                                  const SizedBox(width: 10),
                                  Expanded(
                                    child: Text(
                                      summary.rankedCategories[i].key,
                                    ),
                                  ),
                                  Text(
                                    amount(summary.rankedCategories[i].value),
                                    style: const TextStyle(
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 8),
                              LinearProgressIndicator(
                                value:
                                    summary.rankedCategories[i].value /
                                    summary.expenseMinor,
                                color:
                                    BrandColors.categories[i %
                                        BrandColors.categories.length],
                                backgroundColor: Theme.of(context)
                                    .colorScheme
                                    .surfaceContainerHighest,
                                minHeight: 5,
                                borderRadius: BorderRadius.circular(4),
                              ),
                            ],
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            const SectionTitle('Daily spending'),
            if (summary.dailyExpenses.isNotEmpty)
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      SizedBox(
                        height: 130,
                        child: LayoutBuilder(
                          builder: (context, constraints) {
                            final days = DateTime(
                              month.year,
                              month.month + 1,
                              0,
                            ).day;
                            final maximum = summary.dailyExpenses.values.fold(
                              1,
                              math.max,
                            );
                            return Row(
                              crossAxisAlignment: CrossAxisAlignment.end,
                              children: [
                                for (var day = 1; day <= days; day++)
                                  Expanded(
                                    child: Tooltip(
                                      message:
                                          '${DateFormat('dd MMM').format(DateTime(month.year, month.month, day))}: ${amount(summary.dailyExpenses[day] ?? 0)}',
                                      child: Container(
                                        margin: const EdgeInsets.symmetric(
                                          horizontal: 1,
                                        ),
                                        height:
                                            4 +
                                            120 *
                                                (summary.dailyExpenses[day] ??
                                                    0) /
                                                maximum,
                                        decoration: BoxDecoration(
                                          color:
                                              (summary.dailyExpenses[day] ??
                                                      0) ==
                                                  0
                                              ? Theme.of(context)
                                                    .colorScheme
                                                    .surfaceContainerHighest
                                              : BrandColors.cardBlue,
                                          borderRadius: BorderRadius.circular(
                                            3,
                                          ),
                                        ),
                                      ),
                                    ),
                                  ),
                              ],
                            );
                          },
                        ),
                      ),
                      const SizedBox(height: 12),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('1'),
                          Text(
                            '${DateTime(month.year, month.month + 1, 0).day}',
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            const SizedBox(height: 20),
            OutlinedButton.icon(
              onPressed: () => context.push('/uncategorized'),
              icon: const Icon(Icons.category_outlined),
              label: const Text('Review uncategorized transactions'),
            ),
          ],
        );
      },
    );
  }
}

class _Metric extends StatelessWidget {
  const _Metric({
    required this.label,
    required this.value,
    required this.color,
  });
  final String label;
  final String value;
  final Color color;
  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(18),
      child: SizedBox(
        width: 180,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(label),
            const SizedBox(height: 8),
            Text(
              value,
              style: TextStyle(
                color: color,
                fontSize: 22,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    ),
  );
}

class _DonutPainter extends CustomPainter {
  _DonutPainter(this.values);
  final List<int> values;
  @override
  void paint(Canvas canvas, Size size) {
    final total = values.fold(0, (a, b) => a + b);
    if (total == 0) return;
    final rect = Rect.fromLTWH(12, 12, size.width - 24, size.height - 24);
    var start = -math.pi / 2;
    for (var i = 0; i < values.length; i++) {
      final sweep = values[i] / total * math.pi * 2;
      canvas.drawArc(
        rect,
        start,
        sweep,
        false,
        Paint()
          ..color = BrandColors.categories[i % BrandColors.categories.length]
          ..style = PaintingStyle.stroke
          ..strokeWidth = 18,
      );
      start += sweep;
    }
  }

  @override
  bool shouldRepaint(_DonutPainter oldDelegate) =>
      oldDelegate.values.toString() != values.toString();
}
