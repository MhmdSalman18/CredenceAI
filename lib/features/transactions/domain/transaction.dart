enum TransactionType { debit, credit }

const transactionCategories = [
  'Food & Dining',
  'Shopping',
  'Transport',
  'Entertainment',
  'Health',
  'Bills & Utilities',
  'Travel',
  'Education',
  'Income',
  'Others',
];
const paymentModes = [
  'UPI',
  'Credit Card',
  'Debit Card',
  'Net Banking',
  'Cash',
  'Wallet',
  'SMS',
  'APP',
];

class FinanceTransaction {
  const FinanceTransaction({
    required this.id,
    required this.amountMinor,
    required this.type,
    required this.merchant,
    required this.date,
    this.currency = 'INR',
    this.category,
    this.source = 'MANUAL',
    this.note = '',
    this.paymentMode = 'UPI',
    this.referenceId,
  });

  final String id;
  final int amountMinor;
  final TransactionType type;
  final String merchant;
  final DateTime date;
  final String currency;
  final String? category;
  final String source;
  final String note;
  final String paymentMode;
  final String? referenceId;
  bool get isExpense => type == TransactionType.debit;
  bool get isUncategorized => category == null || category!.isEmpty;

  Map<String, Object?> toMap() => {
    'id': id,
    'amount_minor': amountMinor,
    'type': type.name,
    'merchant': merchant,
    'date_ms': date.millisecondsSinceEpoch,
    'currency': currency,
    'category': category,
    'source': source,
    'note': note,
    'payment_mode': paymentMode,
    'reference_id': referenceId,
  };

  factory FinanceTransaction.fromMap(Map<String, Object?> map) =>
      FinanceTransaction(
        id: map['id']! as String,
        amountMinor: map['amount_minor']! as int,
        type: TransactionType.values.byName(map['type']! as String),
        merchant: map['merchant']! as String,
        date: DateTime.fromMillisecondsSinceEpoch(map['date_ms']! as int),
        currency: map['currency']! as String,
        category: map['category'] as String?,
        source: map['source']! as String,
        note: map['note']! as String,
        paymentMode: map['payment_mode']! as String,
        referenceId: map['reference_id'] as String?,
      );
}
