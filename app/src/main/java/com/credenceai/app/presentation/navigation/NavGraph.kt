package com.credenceai.app.presentation.navigation

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.credenceai.app.presentation.ui.screens.dashboard.DashboardScreen
import com.credenceai.app.presentation.ui.screens.transactions.TransactionsScreen
import com.credenceai.app.presentation.ui.screens.addedit.AddEditTransactionScreen

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.Dashboard.route
    ) {

        composable(ScreenRoutes.Dashboard.route) {
            DashboardScreen(
                onNavigateToAdd = {
                    navController.navigate(ScreenRoutes.AddTransaction.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(ScreenRoutes.Transactions.route)
                }
            )
        }

        composable(ScreenRoutes.Transactions.route) {
            TransactionsScreen()
        }

        composable(ScreenRoutes.AddTransaction.route) {
            AddEditTransactionScreen()
        }
    }
}