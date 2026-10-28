import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../../../core/theme/app_theme.dart';
import '../../../../core/utils/money.dart';
import '../../domain/transaction.dart';

class TransactionTile extends StatelessWidget {
  const TransactionTile(this.transaction, {super.key});
  final FinanceTransaction transaction;
  @override
  Widget build(BuildContext context) {
    final color = transaction.isExpense ? BrandColors.red : BrandColors.green;
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Card(
        child: ListTile(
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 14,
            vertical: 6,
          ),
          leading: CircleAvatar(
            backgroundColor: color.withValues(alpha: .1),
            child: Icon(
              transaction.isExpense ? Icons.south_west : Icons.north_east,
              color: color,
            ),
          ),
          title: Text(
            transaction.merchant,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(fontWeight: FontWeight.w600),
          ),
          subtitle: Text(
            '${transaction.category ?? 'Uncategorized'} · ${DateFormat('dd MMM, h:mm a').format(transaction.date)}',
          ),
          trailing: Text(
            '${transaction.isExpense ? '−' : '+'}${Money.format(transaction.amountMinor, currency: transaction.currency)}',
            style: TextStyle(color: color, fontWeight: FontWeight.bold),
          ),
          onTap: () => context.push('/transaction/${transaction.id}'),
        ),
      ),
    );
  }
}
