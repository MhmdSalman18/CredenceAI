import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:credence_ai_flutter/core/storage/app_database.dart';
import 'package:credence_ai_flutter/features/transactions/data/sqlite_transaction_repository.dart';
import 'package:credence_ai_flutter/features/transactions/domain/transaction.dart';
import 'package:credence_ai_flutter/features/backup/application/backup_service.dart';
import 'package:credence_ai_flutter/features/backup/data/backup_cipher.dart';

void main() {
  late AppDatabase database;
  late SqliteTransactionRepository repository;
  FinanceTransaction record(String id, {String? reference, int amount = 100}) =>
      FinanceTransaction(
        id: id,
        amountMinor: amount,
        merchant: 'Shop',
        type: TransactionType.debit,
        date: DateTime(2026, 10, 28),
        referenceId: reference,
      );
  setUp(() async {
    sqfliteFfiInit();
    database = await AppDatabase.open(
      factory: databaseFactoryFfi,
      path: inMemoryDatabasePath,
    );
    repository = SqliteTransactionRepository(database);
  });
  tearDown(() => database.close());

  test('notification retries do not duplicate records and edits update the original', () async {
    await repository.save(record('first', reference: 'upi-123'));
    await repository.save(record('retry', reference: 'upi-123'));
    expect(await repository.getAll(), hasLength(1));
    await repository.save(record('first', reference: 'upi-123', amount: 250));
    expect((await repository.getAll()).single.amountMinor, 250);
  });

  test(
    'backup round trip restores original records and rejects a wrong password',
    () async {
      await repository.save(record('original'));
      final backups = BackupService(database);
      final bytes = await backups.create('a long test passphrase');
      await repository.save(record('new'));
      await expectLater(backups.restore(bytes, 'incorrect'), throwsException);
      expect(await repository.getAll(), hasLength(2));
      await backups.restore(bytes, 'a long test passphrase');
      expect((await repository.getAll()).single.id, 'original');
    },
  );

  test(
    'tampered ciphertext is rejected without changing existing records',
    () async {
      await repository.save(record('keep'));
      final backups = BackupService(database);
      final bytes = await backups.create('a long test passphrase');
      final envelope = jsonDecode(utf8.decode(bytes)) as Map<String, dynamic>;
      final cipher = base64Decode(envelope['ciphertext'] as String);
      cipher[0] ^= 1;
      envelope['ciphertext'] = base64Encode(cipher);
      await expectLater(
        backups.restore(
          Uint8List.fromList(utf8.encode(jsonEncode(envelope))),
          'a long test passphrase',
        ),
        throwsException,
      );
      expect((await repository.getAll()).single.id, 'keep');
    },
  );

  test('invalid backup content never clears the live database', () async {
    await repository.save(record('keep'));
    final cipher = BackupCipher();
    final invalid = await cipher.encrypt(
      jsonEncode({
        'schema_version': 1,
        'transactions': [record('bad', amount: -1).toMap()],
        'budgets': [],
      }),
      'a long test passphrase',
    );
    await expectLater(
      BackupService(database).restore(invalid, 'a long test passphrase'),
      throwsException,
    );
    expect((await repository.getAll()).single.id, 'keep');
  });
}
