import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:uuid/uuid.dart';

import '../../../core/utils/money.dart';
import '../../../core/widgets/common.dart';
import '../../settings/application/settings_controller.dart';
import '../../transactions/domain/transaction.dart';
import '../application/budgets_controller.dart';
import '../domain/budget.dart';

class BudgetEditorScreen extends ConsumerStatefulWidget {
  const BudgetEditorScreen({required this.id, super.key});
  final String id;
  @override
  ConsumerState<BudgetEditorScreen> createState() => _BudgetEditorScreenState();
}

class _BudgetEditorScreenState extends ConsumerState<BudgetEditorScreen> {
  final form = GlobalKey<FormState>();
  final name = TextEditingController(text: 'My Budget');
  final total = TextEditingController();
  final allocations = <String, TextEditingController>{
    for (final c in [
      'Food & Dining',
      'Transport',
      'Bills & Utilities',
      'Shopping',
    ])
      c: TextEditingController(text: '0.00'),
  };
  bool autoDistribute = true;
  bool repeats = true;
  bool initialized = false;
  bool saving = false;
  Budget? original;
  DateTime month = DateTime(DateTime.now().year, DateTime.now().month);

  @override
  void dispose() {
    name.dispose();
    total.dispose();
    for (final c in allocations.values) {
      c.dispose();
    }
    super.dispose();
  }

  void distribute() {
    final values = Budget.distribute(
      Money.parse(total.text) ?? 0,
      allocations.keys.toList(),
    );
    for (final entry in values.entries) {
      allocations[entry.key]!.text = Money.input(entry.value);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      title: Text(widget.id == 'new' ? 'Create budget' : 'Edit budget'),
    ),
    body: AsyncContent(
      value: ref.watch(budgetsProvider),
      retry: () => ref.invalidate(budgetsProvider),
      builder: (budgets) {
        if (!initialized) {
          initialized = true;
          for (final budget in budgets.where((b) => b.id == widget.id)) {
            original = budget;
            name.text = budget.name;
            total.text = Money.input(budget.totalMinor);
            month = budget.month;
            repeats = budget.repeats;
            autoDistribute = false;
            for (final controller in allocations.values) {
              controller.dispose();
            }
            allocations.clear();
            for (final entry in budget.allocations.entries) {
              allocations[entry.key] = TextEditingController(
                text: Money.input(entry.value),
              );
            }
          }
        }
        if (widget.id != 'new' && original == null) {
          return const EmptyState(
            title: 'Budget not found',
            message: 'Go back to your budgets.',
          );
        }
        final currency =
            original?.currency ?? ref.watch(settingsProvider).currency;
        final assigned = allocations.values.fold(
          0,
          (sum, c) => sum + (Money.parse(c.text) ?? 0),
        );
        return Form(
          key: form,
          child: FormBody(
            children: [
              TextFormField(
                controller: name,
                maxLength: 80,
                decoration: const InputDecoration(labelText: 'Budget name'),
                validator: (v) =>
                    v == null || v.trim().isEmpty ? 'Enter a name' : null,
              ),
              const SizedBox(height: 14),
              TextFormField(
                controller: total,
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
                decoration: InputDecoration(
                  labelText: 'Monthly limit ($currency)',
                ),
                onChanged: (_) => setState(() {
                  if (autoDistribute) distribute();
                }),
                validator: (v) => (Money.parse(v ?? '') ?? 0) <= 0
                    ? 'Enter a positive amount'
                    : null,
              ),
              const SizedBox(height: 18),
              Card(
                child: ListTile(
                  leading: const Icon(Icons.calendar_month),
                  title: Text(DateFormat('MMMM yyyy').format(month)),
                  onTap: () async {
                    final day = await showDatePicker(
                      context: context,
                      initialDate: month,
                      firstDate: DateTime(2000),
                      lastDate: DateTime(2100),
                    );
                    if (day != null) {
                      setState(() => month = DateTime(day.year, day.month));
                    }
                  },
                ),
              ),
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('Repeat every month'),
                value: repeats,
                onChanged: (v) => setState(() => repeats = v),
              ),
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('Distribute equally'),
                subtitle: const Text(
                  'Divide the total across selected categories',
                ),
                value: autoDistribute,
                onChanged: (v) => setState(() {
                  autoDistribute = v;
                  if (v) distribute();
                }),
              ),
              SectionTitle(
                'Category limits',
                action: IconButton(
                  tooltip: 'Add category',
                  icon: const Icon(Icons.add),
                  onPressed: () async {
                    final available = transactionCategories.where(
                      (c) => c != 'Income' && !allocations.containsKey(c),
                    );
                    final selected = await showDialog<String>(
                      context: context,
                      builder: (context) => SimpleDialog(
                        title: const Text('Add category'),
                        children: available
                            .map(
                              (c) => SimpleDialogOption(
                                onPressed: () => Navigator.pop(context, c),
                                child: Text(c),
                              ),
                            )
                            .toList(),
                      ),
                    );
                    if (selected != null && mounted) {
                      setState(() {
                        allocations[selected] = TextEditingController(
                          text: '0.00',
                        );
                        if (autoDistribute) distribute();
                      });
                    }
                  },
                ),
              ),
              for (final entry in allocations.entries)
                Padding(
                  padding: const EdgeInsets.only(bottom: 14),
                  child: Row(
                    children: [
                      Expanded(
                        child: TextFormField(
                          controller: entry.value,
                          readOnly: autoDistribute,
                          keyboardType: const TextInputType.numberWithOptions(
                            decimal: true,
                          ),
                          decoration: InputDecoration(labelText: entry.key),
                          validator: (v) => Money.parse(v ?? '') == null
                              ? 'Enter a valid amount'
                              : null,
                          onChanged: (_) => setState(() {}),
                        ),
                      ),
                      IconButton(
                        tooltip: 'Remove ${entry.key}',
                        onPressed: () => setState(() {
                          allocations.remove(entry.key)?.dispose();
                          if (autoDistribute) distribute();
                        }),
                        icon: const Icon(Icons.close),
                      ),
                    ],
                  ),
                ),
              Text(
                'Assigned: ${Money.format(assigned, currency: currency)} · Unassigned: ${Money.format((Money.parse(total.text) ?? 0) - assigned, currency: currency)}',
              ),
              const SizedBox(height: 24),
              FilledButton.icon(
                onPressed: saving
                    ? null
                    : () async {
                        if (!form.currentState!.validate()) return;
                        setState(() => saving = true);
                        try {
                          await ref
                              .read(budgetsProvider.notifier)
                              .save(
                                Budget(
                                  id: original?.id ?? const Uuid().v4(),
                                  name: name.text.trim(),
                                  totalMinor: Money.parse(total.text)!,
                                  month: month,
                                  allocations: {
                                    for (final entry in allocations.entries)
                                      entry.key: Money.parse(entry.value.text)!,
                                  },
                                  currency: currency,
                                  repeats: repeats,
                                ),
                              );
                          if (context.mounted) context.pop();
                        } catch (e) {
                          if (context.mounted) showError(context, e);
                        } finally {
                          if (mounted) setState(() => saving = false);
                        }
                      },
                icon: const Icon(Icons.check),
                label: Text(saving ? 'Saving…' : 'Save budget'),
              ),
            ],
          ),
        );
      },
    ),
  );
}
