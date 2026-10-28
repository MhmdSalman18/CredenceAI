import 'package:intl/intl.dart';

/// Monetary values are stored as integer minor units, never floating point.
abstract final class Money {
  static int? parse(String text) {
    final value = text.trim().replaceAll(',', '');
    if (!RegExp(r'^\d{1,12}(\.\d{1,2})?$').hasMatch(value)) return null;
    final parts = value.split('.');
    return int.parse(parts[0]) * 100 +
        (parts.length == 2 ? int.parse(parts[1].padRight(2, '0')) : 0);
  }

  static String input(int minor) => (minor / 100).toStringAsFixed(2);

  static String format(int minor, {String currency = 'INR'}) {
    final symbol =
        const {'INR': '₹', 'USD': '\$', 'EUR': '€', 'GBP': '£'}[currency] ??
        currency;
    return NumberFormat.currency(
      locale: currency == 'INR' ? 'en_IN' : 'en_US',
      symbol: symbol,
    ).format(minor / 100);
  }
}
