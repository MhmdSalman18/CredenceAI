package com.credenceai.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.credenceai.app.presentation.ui.screens.addedit.AddEditTransactionScreen
import com.credenceai.app.presentation.ui.screens.add_expense.AddExpenseScreen
import com.credenceai.app.presentation.ui.screens.home.HomeScreen
import com.credenceai.app.presentation.ui.screens.smart_budget.SmartBudgetIntroScreen
import com.credenceai.app.presentation.ui.screens.transactions.TransactionsScreen
import com.credenceai.app.presentation.ui.screens.uncategorized.UncategorizedScreen

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.Home.route
    ) {

        composable(ScreenRoutes.Home.route) {
            HomeScreen(
                onAddExpense = {
                    navController.navigate(ScreenRoutes.AddExpense.route)
                },
                onSmartBudget = {
                    navController.navigate(ScreenRoutes.SmartBudget.route)
                }
            )
        }

        composable(ScreenRoutes.SmartBudget.route) {
            SmartBudgetIntroScreen()
        }

        composable(ScreenRoutes.Transactions.route) {
            TransactionsScreen()
        }

        composable(ScreenRoutes.AddTransaction.route) {
            AddEditTransactionScreen()
        }

        composable(ScreenRoutes.AddExpense.route) {
            AddExpenseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(ScreenRoutes.Uncategorized.route) {
            UncategorizedScreen()
        }
    }
}