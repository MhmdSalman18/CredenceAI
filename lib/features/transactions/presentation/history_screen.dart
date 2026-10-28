import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../../core/widgets/common.dart';
import '../../settings/application/settings_controller.dart';
import '../../backup/application/export_service.dart';
import '../application/transactions_controller.dart';
import '../domain/transaction.dart';
import 'widgets/transaction_tile.dart';

class HistoryScreen extends ConsumerStatefulWidget {
  const HistoryScreen({
    this.uncategorizedOnly = false,
    this.notificationsOnly = false,
    super.key,
  });
  final bool uncategorizedOnly;
  final bool notificationsOnly;
  @override
  ConsumerState<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends ConsumerState<HistoryScreen> {
  String query = '';
  String type = 'All';
  String category = 'All categories';
  DateTime? month;

  @override
  Widget build(BuildContext context) {
    final currency = ref.watch(settingsProvider).currency;
    final body = AsyncContent(
      value: ref.watch(transactionsProvider),
      retry: () => ref.invalidate(transactionsProvider),
      builder: (all) {
        final filtered = all
            .where(
              (t) =>
                  t.currency == currency &&
                  (!widget.uncategorizedOnly || t.isUncategorized) &&
                  (!widget.notificationsOnly || t.source == 'NOTIFICATION') &&
                  (query.isEmpty ||
                      '${t.merchant} ${t.note} ${t.category ?? ''}'
                          .toLowerCase()
                          .contains(query.toLowerCase())) &&
                  (type == 'All' || (type == 'Expense') == t.isExpense) &&
                  (category == 'All categories' || t.category == category) &&
                  (month == null ||
                      t.date.year == month!.year &&
                          t.date.month == month!.month),
            )
            .toList();
        return PageBody(
          children: [
            TextField(
              decoration: const InputDecoration(
                hintText: 'Search transactions',
                prefixIcon: Icon(Icons.search),
              ),
              onChanged: (value) => setState(() => query = value),
            ),
            const SizedBox(height: 14),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                for (final value in ['All', 'Expense', 'Income'])
                  ChoiceChip(
                    label: Text(value),
                    selected: type == value,
                    onSelected: (_) => setState(() => type = value),
                  ),
                ActionChip(
                  avatar: const Icon(Icons.calendar_month, size: 18),
                  label: Text(
                    month == null
                        ? 'All dates'
                        : DateFormat('MMM yyyy').format(month!),
                  ),
                  onPressed: () async {
                    final date = await showDatePicker(
                      context: context,
                      initialDate: month ?? DateTime.now(),
                      firstDate: DateTime(2000),
                      lastDate: DateTime(2100),
                    );
                    if (date != null) setState(() => month = date);
                  },
                ),
                if (month != null)
                  ActionChip(
                    label: const Text('Clear date'),
                    onPressed: () => setState(() => month = null),
                  ),
              ],
            ),
            const SizedBox(height: 14),
            DropdownButtonFormField<String>(
              initialValue: category,
              decoration: const InputDecoration(labelText: 'Category'),
              items: [
                'All categories',
                ...transactionCategories,
              ].map((v) => DropdownMenuItem(value: v, child: Text(v))).toList(),
              onChanged: (value) => setState(() => category = value!),
            ),
            SectionTitle(
              '${filtered.length} transactions',
              action: IconButton(
                tooltip: 'Export filtered transactions',
                icon: const Icon(Icons.file_download_outlined),
                onPressed: () async {
                  try {
                    await ref.read(exportServiceProvider).share(filtered);
                  } catch (e) {
                    if (context.mounted) showError(context, e);
                  }
                },
              ),
            ),
            if (filtered.isEmpty)
              const EmptyState(
                title: 'No transactions found',
                message: 'Adjust your filters or add a transaction.',
              )
            else
              ...filtered.map(TransactionTile.new),
            const SizedBox(height: 20),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                FilledButton.icon(
                  onPressed: () => context.push('/transaction/new'),
                  icon: const Icon(Icons.remove),
                  label: const Text('Add expense'),
                ),
                OutlinedButton.icon(
                  onPressed: () => context.push('/transaction/new?type=credit'),
                  icon: const Icon(Icons.add),
                  label: const Text('Add income'),
                ),
              ],
            ),
          ],
        );
      },
    );
    if (!widget.uncategorizedOnly && !widget.notificationsOnly) return body;
    return Scaffold(
      appBar: AppBar(
        title: Text(
          widget.uncategorizedOnly ? 'Uncategorized' : 'Notifications',
        ),
      ),
      body: body,
    );
  }
}
