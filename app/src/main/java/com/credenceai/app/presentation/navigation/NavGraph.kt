package com.credenceai.app.presentation.navigation

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.credenceai.app.presentation.ui.screens.dashboard.DashboardScreen
import com.credenceai.app.presentation.ui.screens.transactions.TransactionsScreen
import com.credenceai.app.presentation.ui.screens.addedit.AddEditTransactionScreen
import com.credenceai.app.presentation.ui.screens.home.HomeScreen
import com.credenceai.app.presentation.ui.screens.uncategorized.UncategorizedScreen

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.Home.route
    ) {



        composable(ScreenRoutes.Home.route) {
            HomeScreen()
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