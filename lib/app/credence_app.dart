import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/theme/app_theme.dart';
import '../features/settings/application/settings_controller.dart';
import 'app_services.dart';
import 'router.dart';

class CredenceApp extends ConsumerWidget {
  const CredenceApp({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) => MaterialApp.router(
    title: 'Credence AI',
    debugShowCheckedModeBanner: false,
    theme: appTheme(Brightness.light),
    darkTheme: appTheme(Brightness.dark),
    themeMode: ref.watch(settingsProvider).darkMode
        ? ThemeMode.dark
        : ThemeMode.light,
    routerConfig: ref.watch(routerProvider),
    builder: (context, child) =>
        AppServices(child: child ?? const SizedBox.shrink()),
  );
}
