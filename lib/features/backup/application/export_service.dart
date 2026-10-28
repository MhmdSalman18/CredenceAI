import 'dart:convert';
import 'dart:io';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';

import '../../transactions/domain/transaction.dart';
import '../../../core/utils/money.dart';

final exportServiceProvider = Provider((ref) => ExportService());

class ExportService {
  String csv(List<FinanceTransaction> transactions) {
    // Prevent spreadsheet applications interpreting user-controlled text as formulas.
    String cell(String value) {
      final safe = RegExp(r'^\s*[=+@\-\t\r]').hasMatch(value)
          ? "'$value"
          : value;
      return '"${safe.replaceAll('"', '""')}"';
    }

    return [
      'Date,Type,Merchant,Amount,Currency,Category,Payment method,Source,Notes',
      ...transactions.map(
        (t) => [
          t.date.toIso8601String(),
          t.type.name,
          t.merchant,
          Money.input(t.amountMinor),
          t.currency,
          t.category ?? 'Uncategorized',
          t.paymentMode,
          t.source,
          t.note,
        ].map(cell).join(','),
      ),
    ].join('\r\n');
  }

  Future<void> share(List<FinanceTransaction> transactions) async {
    final directory = await getTemporaryDirectory();
    final file = File(
      p.join(
        directory.path,
        'credence_transactions_${DateTime.now().millisecondsSinceEpoch}.csv',
      ),
    );
    await file.writeAsBytes([
      0xef,
      0xbb,
      0xbf,
      ...utf8.encode(csv(transactions)),
    ], flush: true);
    await SharePlus.instance.share(
      ShareParams(
        files: [XFile(file.path)],
        subject: 'Credence AI transactions',
      ),
    );
  }
}
