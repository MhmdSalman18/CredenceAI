import 'dart:io';

import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';

class AppDatabase {
  AppDatabase._(this.connection);
  final Database connection;
  static const version = 1;

  static Future<AppDatabase> open({
    DatabaseFactory? factory,
    String? path,
  }) async {
    var selected = factory ?? databaseFactory;
    if (factory == null && (Platform.isWindows || Platform.isLinux)) {
      sqfliteFfiInit();
      selected = databaseFactoryFfi;
    }
    final location =
        path ??
        p.join(
          (await getApplicationSupportDirectory()).path,
          'credence_flutter.db',
        );
    final database = await selected.openDatabase(
      location,
      options: OpenDatabaseOptions(
        version: version,
        onConfigure: (db) async {
          await db.execute('PRAGMA foreign_keys = ON');
        },
        onCreate: (db, version) async {
          await db.execute('''CREATE TABLE transactions (
          id TEXT PRIMARY KEY, amount_minor INTEGER NOT NULL CHECK(amount_minor > 0),
          type TEXT NOT NULL CHECK(type IN ('debit','credit')), merchant TEXT NOT NULL,
          date_ms INTEGER NOT NULL, currency TEXT NOT NULL, category TEXT,
          source TEXT NOT NULL, note TEXT NOT NULL, payment_mode TEXT NOT NULL,
          reference_id TEXT UNIQUE)''');
          await db.execute(
            'CREATE INDEX transactions_date ON transactions(date_ms DESC)',
          );
          await db.execute(
            '''CREATE TABLE budgets (
          id TEXT PRIMARY KEY, name TEXT NOT NULL,
          total_minor INTEGER NOT NULL CHECK(total_minor > 0), month_ms INTEGER NOT NULL,
          allocations TEXT NOT NULL, currency TEXT NOT NULL, repeats INTEGER NOT NULL)''',
          );
        },
        // Add explicit, non-destructive migration steps when increasing version.
        onUpgrade: (db, oldVersion, newVersion) async {
          throw StateError('No migration from $oldVersion to $newVersion');
        },
      ),
    );
    return AppDatabase._(database);
  }

  Future<void> close() => connection.close();
}
