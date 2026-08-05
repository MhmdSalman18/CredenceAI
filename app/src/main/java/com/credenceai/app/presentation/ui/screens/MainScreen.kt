package com.credenceai.app.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.credenceai.app.R
import com.credenceai.app.presentation.navigation.*
import com.credenceai.app.presentation.ui.screens.add_expense.AddExpenseScreen
import com.credenceai.app.presentation.ui.screens.addedit.AddEditTransactionScreen
import com.credenceai.app.presentation.ui.screens.analytics.AnalyticsScreen
import com.credenceai.app.presentation.ui.screens.history.HistoryScreen
import com.credenceai.app.presentation.ui.screens.home.HomeScreen
import com.credenceai.app.presentation.ui.screens.notifications.NotificationsScreen
import com.credenceai.app.presentation.ui.screens.settings.SettingsScreen
import com.credenceai.app.presentation.ui.screens.backup.BackupScreen
import com.credenceai.app.presentation.ui.screens.smart_budget.EditBudgetScreen
import com.credenceai.app.presentation.ui.screens.smart_budget.SmartBudgetIntroScreen
import com.credenceai.app.presentation.ui.screens.smart_budget.ViewBudgetScreen
import com.credenceai.app.presentation.ui.screens.supported_banks.SupportedBanksScreen
import com.credenceai.app.presentation.ui.screens.transactions.TransactionsScreen
import com.credenceai.app.presentation.ui.screens.uncategorized.UncategorizedScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: com.credenceai.app.presentation.MainViewModel = hiltViewModel()
) {
    val startDestination by viewModel.startDestination.collectAsState()

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Analytics,
        BottomNavItem.Settings
    )

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Only hide bars for full-screen add/edit flows
    val isAddScreen = currentRoute == ScreenRoutes.AddExpense.route ||
            currentRoute == ScreenRoutes.AddIncome.route

    val showBottomBar = !isAddScreen
    val showTopBar = !isAddScreen

    val topBarTitle = when (currentRoute) {
        BottomNavItem.Home.route -> ""
        BottomNavItem.History.route -> "History"
        BottomNavItem.Analytics.route -> "Analytics"
        BottomNavItem.Settings.route -> "Settings"
        ScreenRoutes.Notifications.route -> "Notifications"
        ScreenRoutes.SmartBudget.route -> "Smart Budget"
        else -> "CredenceAI"
    }

    val showLogo = currentRoute == BottomNavItem.Home.route ||
            currentRoute == ScreenRoutes.Home.route

    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text(topBarTitle) },
                    navigationIcon = {
                        if (showLogo) {
                            Icon(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(120.dp),
                                tint = Color.Unspecified
                            )
                        }
                    },
                    actions = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },

        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = backgroundColor,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Use the graph's start destination ID so the pop
                                    // always targets the true root. Don't save/restore state for Home
                                    // so clicking Dashboard always goes straight to the home screen.
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = item.route != BottomNavItem.Home.route
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                    restoreState = item.route != BottomNavItem.Home.route
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        },
        containerColor = backgroundColor
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = startDestination!!,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(ScreenRoutes.Home.route) {
                HomeScreen(
                    onAddExpense = {
                        navController.navigate(ScreenRoutes.AddExpense.route)
                    },
                    onSmartBudget = {
                        // Navigate to SmartBudget WITHOUT saving Home's back-stack state
                        // so that when the user switches tabs and returns to Dashboard,
                        // the restored state is just HomeScreen — not Home + SmartBudget.
                        navController.navigate(ScreenRoutes.SmartBudget.route) {
                            popUpTo("home") {
                                inclusive = false
                                saveState = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onViewAllTransactions = {
                        navController.navigate("history")
                    },
                    onNotificationsClick = {
                        navController.navigate(ScreenRoutes.Notifications.route)
                    },
                    onNavigateToUncategorized = {
                        navController.navigate(ScreenRoutes.Uncategorized.route)
                    },
                    onEditTransaction = { id, amount, merchant, timestamp, type ->
                        val encodedMerchant = URLEncoder.encode(merchant, StandardCharsets.UTF_8.toString())
                        navController.navigate(
                            "${ScreenRoutes.AddExpense.route}?id=$id&amount=$amount&merchant=$encodedMerchant&timestamp=$timestamp&type=$type"
                        )
                    }
                )
            }

            // ── Add Transaction (full screen, no bottom bar) ──────────────────
            composable(
                route = "${ScreenRoutes.AddExpense.route}?id={id}&amount={amount}&merchant={merchant}&timestamp={timestamp}&type={type}",
                arguments = listOf(
                    navArgument("id") { defaultValue = "" },
                    navArgument("amount") { defaultValue = "" },
                    navArgument("merchant") { defaultValue = "" },
                    navArgument("timestamp") { defaultValue = 0L },
                    navArgument("type") { defaultValue = "debit" }
                )
            ) { backStackEntry ->
                val idStr = backStackEntry.arguments?.getString("id") ?: ""
                val id = idStr.toIntOrNull()
                val amount = backStackEntry.arguments?.getString("amount") ?: ""
                val merchant = backStackEntry.arguments?.getString("merchant") ?: ""
                val timestamp = backStackEntry.arguments?.getLong("timestamp") ?: 0L
                val type = backStackEntry.arguments?.getString("type") ?: "debit"

                AddExpenseScreen(
                    isIncome = type.lowercase() == "credit" || type.lowercase() == "income",
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() },
                    initialId = id,
                    initialAmount = amount,
                    initialMerchant = merchant,
                    initialTimestamp = if (timestamp != 0L) timestamp else null
                )
            }

            composable(ScreenRoutes.AddExpense.route) {
                AddExpenseScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            composable(ScreenRoutes.Transactions.route) { TransactionsScreen() }
            composable(ScreenRoutes.AddTransaction.route) { AddEditTransactionScreen() }

            composable(ScreenRoutes.Uncategorized.route) {
                UncategorizedScreen(
                    onEditTransaction = { id, amount, merchant, timestamp, type ->
                        val encodedMerchant = URLEncoder.encode(merchant, StandardCharsets.UTF_8.toString())
                        navController.navigate(
                            "${ScreenRoutes.AddExpense.route}?id=$id&amount=$amount&merchant=$encodedMerchant&timestamp=$timestamp&type=$type"
                        )
                    }
                )
            }

            composable("history") {
                HistoryScreen(
                    onAddExpense = {
                        navController.navigate(ScreenRoutes.AddExpense.route)
                    },
                    onAddIncome = {
                        navController.navigate(ScreenRoutes.AddIncome.route)
                    },
                    onEditTransaction = { id, amount, merchant, timestamp, type ->
                        val encodedMerchant = URLEncoder.encode(merchant, StandardCharsets.UTF_8.toString())
                        navController.navigate(
                            "${ScreenRoutes.AddExpense.route}?id=$id&amount=$amount&merchant=$encodedMerchant&timestamp=$timestamp&type=$type"
                        )
                    }
                )
            }

            composable("analytics") {
                AnalyticsScreen(
                    onNavigateToUncategorized = {
                        navController.navigate(ScreenRoutes.Uncategorized.route)
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onNavigateToSupportedBanks = {
                        navController.navigate(ScreenRoutes.SupportedBanks.route)
                    },
                    onNavigateToUncategorized = {
                        navController.navigate(ScreenRoutes.Uncategorized.route)
                    },
                    onNavigateToBackup = {
                        navController.navigate(ScreenRoutes.Backup.route)
                    }
                )
            }

            composable(ScreenRoutes.SupportedBanks.route) {
                SupportedBanksScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(ScreenRoutes.Notifications.route) {
                NotificationsScreen(
                    onBackClick = { navController.popBackStack() },
                    onEditTransaction = { id, amount, merchant, timestamp, type ->
                        val encodedMerchant = URLEncoder.encode(merchant, StandardCharsets.UTF_8.toString())
                        navController.navigate(
                            "${ScreenRoutes.AddExpense.route}?id=$id&amount=$amount&merchant=$encodedMerchant&timestamp=$timestamp&type=$type"
                        )
                    }
                )
            }

            composable(ScreenRoutes.SmartBudget.route) {
                SmartBudgetIntroScreen(
                    onSetMonthlyBudget = {
                        navController.navigate(ScreenRoutes.EditBudget.createRoute("new"))
                    },
                    onViewBudget = { budgetId ->
                        navController.navigate(ScreenRoutes.ViewBudget.createRoute(budgetId))
                    },
                    onEditBudget = { budgetId ->
                        navController.navigate(ScreenRoutes.EditBudget.createRoute(budgetId))
                    },
                    onLearnHowItWorks = {
                        // For now, navigate to analytics as a placeholder or show a message
                        // Alternatively, we could have a dedicated help screen
                    }
                )
            }

            composable(
                route = ScreenRoutes.EditBudget.route,
                arguments = listOf(
                    navArgument("budgetId") {
                        type = androidx.navigation.NavType.StringType
                        defaultValue = "new"
                    }
                )
            ) {
                EditBudgetScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onBudgetSaved = {
                        // Return to intro screen to refresh list
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = ScreenRoutes.ViewBudget.route,
                arguments = listOf(
                    navArgument("budgetId") {
                        type = androidx.navigation.NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val budgetId = backStackEntry.arguments?.getString("budgetId") ?: ""
                ViewBudgetScreen(
                    onEditBudget = {
                        navController.navigate(ScreenRoutes.EditBudget.createRoute(budgetId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditTransaction = { id, amount, merchant, timestamp, type ->
                        val encodedMerchant = URLEncoder.encode(merchant, StandardCharsets.UTF_8.toString())
                        navController.navigate(
                            "${ScreenRoutes.AddExpense.route}?id=$id&amount=$amount&merchant=$encodedMerchant&timestamp=$timestamp&type=$type"
                        )
                    },
                    onViewAnalytics = {
                        navController.navigate("analytics")
                    }
                )
            }
            composable(ScreenRoutes.Backup.route) {
                BackupScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}