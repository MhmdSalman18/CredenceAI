import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../features/analytics/presentation/analytics_screen.dart';
import '../features/backup/presentation/backup_screen.dart';
import '../features/budgets/presentation/budget_editor_screen.dart';
import '../features/budgets/presentation/budgets_screen.dart';
import '../features/chat/presentation/chat_screen.dart';
import '../features/dashboard/presentation/dashboard_screen.dart';
import '../features/notifications/presentation/supported_banks_screen.dart';
import '../features/settings/presentation/settings_screen.dart';
import '../features/transactions/presentation/history_screen.dart';
import '../features/transactions/presentation/transaction_editor_screen.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final router = GoRouter(
    initialLocation: '/',
    routes: [
      StatefulShellRoute.indexedStack(
        builder: (context, state, shell) => _AppShell(shell: shell),
        branches: [
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/',
                builder: (context, state) => const DashboardScreen(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/history',
                builder: (context, state) => const HistoryScreen(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/chat',
                builder: (context, state) => const ChatScreen(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/analytics',
                builder: (context, state) => const AnalyticsScreen(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/settings',
                builder: (context, state) => const SettingsScreen(),
              ),
            ],
          ),
        ],
      ),
      GoRoute(
        path: '/transaction/:id',
        builder: (context, state) => TransactionEditorScreen(
          id: state.pathParameters['id']!,
          income: state.uri.queryParameters['type'] == 'credit',
        ),
      ),
      GoRoute(
        path: '/budgets',
        builder: (context, state) => const BudgetsScreen(),
      ),
      GoRoute(
        path: '/budgets/:id/edit',
        builder: (context, state) =>
            BudgetEditorScreen(id: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/budgets/:id',
        builder: (context, state) =>
            BudgetDetailScreen(id: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/backup',
        builder: (context, state) => const BackupScreen(),
      ),
      GoRoute(
        path: '/uncategorized',
        builder: (context, state) =>
            const HistoryScreen(uncategorizedOnly: true),
      ),
      GoRoute(
        path: '/notifications',
        builder: (context, state) =>
            const HistoryScreen(notificationsOnly: true),
      ),
      GoRoute(
        path: '/supported-banks',
        builder: (context, state) => const SupportedBanksScreen(),
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      appBar: AppBar(title: const Text('Page not found')),
      body: Center(
        child: FilledButton(
          onPressed: () => context.go('/'),
          child: const Text('Back to dashboard'),
        ),
      ),
    ),
  );
  ref.onDispose(router.dispose);
  return router;
});

class _AppShell extends StatelessWidget {
  const _AppShell({required this.shell});
  final StatefulNavigationShell shell;
  static const labels = [
    'Dashboard',
    'History',
    'Crence',
    'Analytics',
    'Settings',
  ];
  static const icons = [
    Icons.dashboard_outlined,
    Icons.receipt_long_outlined,
    Icons.auto_awesome_outlined,
    Icons.insights_outlined,
    Icons.settings_outlined,
  ];
  void select(int index) => shell.goBranch(
    index,
    initialLocation: index == shell.currentIndex || index == 0,
  );
  @override
  Widget build(BuildContext context) {
    final wide = MediaQuery.sizeOf(context).width >= 850;
    return Scaffold(
      appBar: AppBar(
        title: shell.currentIndex == 0
            ? Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Image.asset(
                    'assets/images/credenceai_icon.png',
                    width: 34,
                    height: 34,
                  ),
                  const SizedBox(width: 10),
                  const Text(
                    'Credence AI',
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                ],
              )
            : Text(labels[shell.currentIndex]),
      ),
      body: Row(
        children: [
          if (wide)
            NavigationRail(
              selectedIndex: shell.currentIndex,
              onDestinationSelected: select,
              labelType: NavigationRailLabelType.all,
              destinations: [
                for (var i = 0; i < labels.length; i++)
                  NavigationRailDestination(
                    icon: Icon(icons[i]),
                    label: Text(labels[i]),
                  ),
              ],
            ),
          Expanded(child: shell),
        ],
      ),
      bottomNavigationBar: wide
          ? null
          : NavigationBar(
              selectedIndex: shell.currentIndex,
              onDestinationSelected: select,
              destinations: [
                for (var i = 0; i < labels.length; i++)
                  NavigationDestination(icon: Icon(icons[i]), label: labels[i]),
              ],
            ),
    );
  }
}
