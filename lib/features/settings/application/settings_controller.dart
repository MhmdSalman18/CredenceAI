import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/di/dependencies.dart';
import '../domain/app_settings.dart';

final settingsProvider = NotifierProvider<SettingsController, AppSettings>(
  SettingsController.new,
);

class SettingsController extends Notifier<AppSettings> {
  @override
  AppSettings build() {
    final prefs = ref.watch(preferencesProvider);
    return AppSettings(
      userName: prefs.getString('user_name') ?? 'New User',
      darkMode: prefs.getBool('dark_mode') ?? false,
      tracking: prefs.getBool('tracking') ?? true,
      hideBalance: prefs.getBool('hide_balance') ?? false,
      appLock: prefs.getBool('app_lock') ?? false,
      currency: prefs.getString('currency') ?? 'INR',
    );
  }

  Future<void> update(AppSettings value) async {
    final prefs = ref.read(preferencesProvider);
    await Future.wait([
      prefs.setString('user_name', value.userName),
      prefs.setBool('dark_mode', value.darkMode),
      prefs.setBool('tracking', value.tracking),
      prefs.setBool('hide_balance', value.hideBalance),
      prefs.setBool('app_lock', value.appLock),
      prefs.setString('currency', value.currency),
    ]);
    state = value;
  }
}
