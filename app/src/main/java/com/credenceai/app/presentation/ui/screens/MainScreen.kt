package com.credenceai.app.presentation.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.credenceai.app.presentation.navigation.*
import com.credenceai.app.presentation.ui.screens.add_expense.AddExpenseScreen
import com.credenceai.app.presentation.ui.screens.addedit.AddEditTransactionScreen
import com.credenceai.app.presentation.ui.screens.history.HistoryScreen
import com.credenceai.app.presentation.ui.screens.home.HomeScreen
import com.credenceai.app.presentation.ui.screens.transactions.TransactionsScreen
import com.credenceai.app.presentation.ui.screens.uncategorized.UncategorizedScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Analytics,
        BottomNavItem.Settings
    )

    // Track current route to hide bottom bar on AddExpense screen
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showBottomBar = currentRoute != ScreenRoutes.AddExpense.route

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CredenceAI") })
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(ScreenRoutes.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon  = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController    = navController,
            startDestination = ScreenRoutes.Home.route,
            modifier         = Modifier.padding(innerPadding)
        ) {

            composable(ScreenRoutes.Home.route) {
                HomeScreen(
                    onAddExpense = {
                        navController.navigate(ScreenRoutes.AddExpense.route)
                    }
                )
            }

            // ── Add Expense (full screen, no bottom bar) ──────────────────
            composable(ScreenRoutes.AddExpense.route) {
                AddExpenseScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess  = { navController.popBackStack() }
                )
            }

            composable(ScreenRoutes.Transactions.route) { TransactionsScreen() }
            composable(ScreenRoutes.AddTransaction.route) { AddEditTransactionScreen() }
            composable(ScreenRoutes.Uncategorized.route) { UncategorizedScreen() }

            composable("history")   { HistoryScreen() }
            composable("analytics") { TransactionsScreen() }
            composable("settings")  { TransactionsScreen() }
        }
    }
}