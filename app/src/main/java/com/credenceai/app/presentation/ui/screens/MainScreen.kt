package com.credenceai.app.presentation.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.*
import com.credenceai.app.presentation.navigation.*
import com.credenceai.app.presentation.ui.screens.dashboard.DashboardScreen
import com.credenceai.app.presentation.ui.screens.transactions.TransactionsScreen
import com.credenceai.app.presentation.ui.screens.addedit.AddEditTransactionScreen
import com.credenceai.app.presentation.ui.screens.uncategorized.UncategorizedScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Transactions,
        BottomNavItem.Add
    )

    Scaffold(

        // 🔝 TOP BAR
        topBar = {
            TopAppBar(
                title = { Text("CredenceAI") }
            )
        },

        // 🔽 BOTTOM BAR
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate(item.route)
                        },
                        icon = {
                            Icon(item.icon, contentDescription = item.label)
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }

    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = ScreenRoutes.Dashboard.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {

            composable(ScreenRoutes.Dashboard.route) {
                DashboardScreen(
                    onNavigateToAdd = {
                        navController.navigate(ScreenRoutes.AddTransaction.route)
                    },
                    onNavigateToTransactions = {
                        navController.navigate(ScreenRoutes.Transactions.route)
                    },
                    onNavigateToUncategorized = {
                        navController.navigate(ScreenRoutes.Uncategorized.route)
                    }
                )
            }

            composable(ScreenRoutes.Transactions.route) {
                TransactionsScreen()
            }

            composable(ScreenRoutes.AddTransaction.route) {
                AddEditTransactionScreen()
            }
            composable(ScreenRoutes.Uncategorized.route) {
                UncategorizedScreen()
            }
        }
    }
}