import 'package:flutter/material.dart';

abstract final class BrandColors {
  static const blue = Color(0xFF2A1DC4);
  static const cardBlue = Color(0xFF2D5BE3);
  static const cardBlueEnd = Color(0xFF1A3BAA);
  static const green = Color(0xFF27AE60);
  static const red = Color(0xFFE05252);
  static const categories = [
    Color(0xFFE07B39),
    Color(0xFF7B5EA7),
    Color(0xFF2D9CDB),
    Color(0xFF27AE60),
    Color(0xFFDAA520),
    Color(0xFFE05252),
  ];
}

ThemeData appTheme(Brightness brightness) {
  final dark = brightness == Brightness.dark;
  final scheme = ColorScheme.fromSeed(
    seedColor: BrandColors.blue,
    brightness: brightness,
  );
  return ThemeData(
    useMaterial3: true,
    colorScheme: scheme,
    scaffoldBackgroundColor: dark
        ? const Color(0xFF11131E)
        : const Color(0xFFF5F5FA),
    appBarTheme: AppBarTheme(
      backgroundColor: dark ? const Color(0xFF11131E) : const Color(0xFFF5F5FA),
      centerTitle: false,
      elevation: 0,
    ),
    cardTheme: CardThemeData(
      elevation: 0,
      margin: EdgeInsets.zero,
      color: dark ? const Color(0xFF1D2030) : Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
        side: BorderSide(color: scheme.outlineVariant.withValues(alpha: .5)),
      ),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: scheme.surface,
      border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        minimumSize: const Size(48, 50),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      ),
    ),
    navigationBarTheme: NavigationBarThemeData(
      backgroundColor: dark ? const Color(0xFF11131E) : Colors.white,
    ),
  );
}
