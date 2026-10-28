import 'package:flutter/material.dart';

import '../../../core/widgets/common.dart';

class SupportedBanksScreen extends StatelessWidget {
  const SupportedBanksScreen({super.key});
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Supported banks & apps')),
    body: PageBody(
      children: [
        const Text(
          'Credence AI detects transactions from notifications on Android. Results depend on each app’s notification format and visible transaction details.',
        ),
        const SectionTitle('UPI & payment apps'),
        ...[
          'Google Pay',
          'PhonePe',
          'BHIM',
          'Amazon Pay',
          'SuperMoney',
          'Paytm',
          'MobiKwik',
          'FreeCharge',
          'CRED',
          'Slice',
        ].map(
          (name) => Card(
            child: ListTile(
              leading: const Icon(Icons.payments_outlined),
              title: Text(name),
            ),
          ),
        ),
        const SectionTitle('Bank apps'),
        ...[
          'SBI YONO',
          'Kotak Mahindra',
          'Axis Bank',
          'ICICI iMobile',
          'HDFC Bank',
          'IndusInd',
          'IDBI',
          'Bank of India',
          'Punjab National Bank',
        ].map(
          (name) => Card(
            child: ListTile(
              leading: const Icon(Icons.account_balance_outlined),
              title: Text(name),
            ),
          ),
        ),
        const SectionTitle('Bank SMS notifications'),
        const Card(
          child: Padding(
            padding: EdgeInsets.all(18),
            child: Text(
              'Google Messages, Samsung Messages, and Android messaging apps are supported when the notification sender matches a recognised bank ID. OTPs and promotional messages are ignored.',
            ),
          ),
        ),
      ],
    ),
  );
}
