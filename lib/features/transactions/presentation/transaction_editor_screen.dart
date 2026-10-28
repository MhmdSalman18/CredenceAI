import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:uuid/uuid.dart';

import '../../../core/utils/money.dart';
import '../../../core/widgets/common.dart';
import '../../settings/application/settings_controller.dart';
import '../application/transactions_controller.dart';
import '../domain/transaction.dart';

class TransactionEditorScreen extends ConsumerStatefulWidget {
  const TransactionEditorScreen({
    required this.id,
    this.income = false,
    super.key,
  });
  final String id;
  final bool income;
  @override
  ConsumerState<TransactionEditorScreen> createState() =>
      _TransactionEditorScreenState();
}

class _TransactionEditorScreenState
    extends ConsumerState<TransactionEditorScreen> {
  final form = GlobalKey<FormState>();
  final amount = TextEditingController();
  final merchant = TextEditingController();
  final note = TextEditingController();
  late TransactionType type;
  String? category;
  String payment = 'UPI';
  DateTime date = DateTime.now();
  FinanceTransaction? original;
  bool initialized = false;
  bool saving = false;

  @override
  void initState() {
    super.initState();
    type = widget.income ? TransactionType.credit : TransactionType.debit;
    if (widget.income) category = 'Income';
  }

  @override
  void dispose() {
    amount.dispose();
    merchant.dispose();
    note.dispose();
    super.dispose();
  }

  void initialize(List<FinanceTransaction> list) {
    if (initialized) return;
    initialized = true;
    for (final t in list) {
      if (t.id == widget.id) {
        original = t;
        amount.text = Money.input(t.amountMinor);
        merchant.text = t.merchant;
        note.text = t.note;
        type = t.type;
        category = transactionCategories.contains(t.category)
            ? t.category
            : null;
        payment = paymentModes.contains(t.paymentMode) ? t.paymentMode : 'APP';
        date = t.date;
        break;
      }
    }
  }

  Future<void> save() async {
    if (!form.currentState!.validate()) return;
    setState(() => saving = true);
    try {
      await ref
          .read(transactionsProvider.notifier)
          .save(
            FinanceTransaction(
              id: original?.id ?? const Uuid().v4(),
              amountMinor: Money.parse(amount.text)!,
              type: type,
              merchant: merchant.text.trim(),
              date: date,
              category: category,
              paymentMode: payment,
              note: note.text.trim(),
              currency:
                  original?.currency ?? ref.read(settingsProvider).currency,
              source: original?.source ?? 'MANUAL',
              referenceId: original?.referenceId,
            ),
          );
      if (mounted) context.pop();
    } catch (e) {
      if (mounted) showError(context, e);
    } finally {
      if (mounted) setState(() => saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      title: Text(widget.id == 'new' ? 'Add transaction' : 'Edit transaction'),
      actions: [
        if (widget.id != 'new')
          IconButton(
            tooltip: 'Delete transaction',
            onPressed: saving
                ? null
                : () async {
                    if (!await confirmAction(
                      context,
                      title: 'Delete transaction?',
                      message:
                          'This transaction will be removed from your records.',
                    )) {
                      return;
                    }
                    try {
                      await ref
                          .read(transactionsProvider.notifier)
                          .delete(widget.id);
                      if (context.mounted) context.pop();
                    } catch (e) {
                      if (context.mounted) showError(context, e);
                    }
                  },
            icon: const Icon(Icons.delete_outline),
          ),
      ],
    ),
    body: AsyncContent(
      value: ref.watch(transactionsProvider),
      retry: () => ref.invalidate(transactionsProvider),
      builder: (list) {
        initialize(list);
        if (widget.id != 'new' && original == null) {
          return const Center(
            child: EmptyState(
              title: 'Transaction not found',
              message: 'It may have been removed.',
            ),
          );
        }
        return Form(
          key: form,
          child: FormBody(
            children: [
              SegmentedButton<TransactionType>(
                segments: const [
                  ButtonSegment(
                    value: TransactionType.debit,
                    label: Text('Expense'),
                    icon: Icon(Icons.south_west),
                  ),
                  ButtonSegment(
                    value: TransactionType.credit,
                    label: Text('Income'),
                    icon: Icon(Icons.north_east),
                  ),
                ],
                selected: {type},
                onSelectionChanged: saving
                    ? null
                    : (values) => setState(() {
                        type = values.first;
                        if (type == TransactionType.credit) {
                          category = 'Income';
                        } else if (category == 'Income') {
                          category = null;
                        }
                      }),
              ),
              const SizedBox(height: 24),
              TextFormField(
                controller: amount,
                autofocus: widget.id == 'new',
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
                inputFormatters: [
                  FilteringTextInputFormatter.allow(RegExp(r'[\d.,]')),
                ],
                decoration: InputDecoration(
                  labelText:
                      'Amount (${original?.currency ?? ref.watch(settingsProvider).currency})',
                ),
                validator: (value) => (Money.parse(value ?? '') ?? 0) <= 0
                    ? 'Enter an amount greater than zero, with up to two decimals'
                    : null,
              ),
              const SizedBox(height: 18),
              TextFormField(
                controller: merchant,
                maxLength: 120,
                textCapitalization: TextCapitalization.words,
                decoration: InputDecoration(
                  labelText: type == TransactionType.debit
                      ? 'Merchant'
                      : 'Income source',
                ),
                validator: (value) => value == null || value.trim().isEmpty
                    ? 'Enter a name'
                    : null,
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<String>(
                key: ValueKey('category-$type-$category'),
                initialValue: category,
                decoration: const InputDecoration(labelText: 'Category'),
                items: transactionCategories
                    .map((v) => DropdownMenuItem(value: v, child: Text(v)))
                    .toList(),
                onChanged: saving
                    ? null
                    : (value) => setState(() => category = value),
                validator: (value) =>
                    value == null ? 'Choose a category' : null,
              ),
              const SizedBox(height: 18),
              DropdownButtonFormField<String>(
                initialValue: payment,
                decoration: const InputDecoration(labelText: 'Payment method'),
                items: paymentModes
                    .map((v) => DropdownMenuItem(value: v, child: Text(v)))
                    .toList(),
                onChanged: saving
                    ? null
                    : (value) => setState(() => payment = value!),
              ),
              const SizedBox(height: 18),
              Card(
                child: ListTile(
                  leading: const Icon(Icons.calendar_today_outlined),
                  title: Text(DateFormat('dd MMM yyyy, h:mm a').format(date)),
                  trailing: const Icon(Icons.edit_outlined),
                  onTap: saving
                      ? null
                      : () async {
                          final day = await showDatePicker(
                            context: context,
                            initialDate: date,
                            firstDate: DateTime(2000),
                            lastDate: DateTime(2100),
                          );
                          if (day == null || !context.mounted) return;
                          final time = await showTimePicker(
                            context: context,
                            initialTime: TimeOfDay.fromDateTime(date),
                          );
                          if (time != null && mounted) {
                            setState(
                              () => date = DateTime(
                                day.year,
                                day.month,
                                day.day,
                                time.hour,
                                time.minute,
                              ),
                            );
                          }
                        },
                ),
              ),
              const SizedBox(height: 18),
              TextFormField(
                controller: note,
                maxLength: 2000,
                maxLines: 3,
                decoration: const InputDecoration(
                  labelText: 'Notes (optional)',
                  alignLabelWithHint: true,
                ),
              ),
              const SizedBox(height: 24),
              FilledButton.icon(
                onPressed: saving ? null : save,
                icon: saving
                    ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.check),
                label: Text(saving ? 'Saving…' : 'Save transaction'),
              ),
            ],
          ),
        );
      },
    ),
  );
}
