package com.credenceai.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.credenceai.app.presentation.ui.screens.addedit.AddEditTransactionScreen
import com.credenceai.app.presentation.ui.screens.add_expense.AddExpenseScreen
import com.credenceai.app.presentation.ui.screens.home.HomeScreen
import com.credenceai.app.presentation.ui.screens.smart_budget.EditBudgetScreen
import com.credenceai.app.presentation.ui.screens.smart_budget.SmartBudgetIntroScreen
import com.credenceai.app.presentation.ui.screens.smart_budget.ViewBudgetScreen
import com.credenceai.app.presentation.ui.screens.transactions.TransactionsScreen
import com.credenceai.app.presentation.ui.screens.uncategorized.UncategorizedScreen
import com.credenceai.app.presentation.ui.screens.backup.BackupScreen

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
            SmartBudgetIntroScreen(
                onSetMonthlyBudget = {
                    navController.navigate(ScreenRoutes.EditBudget.createRoute("new"))
                },
                onViewBudget = { budgetId ->
                    navController.navigate(ScreenRoutes.ViewBudget.createRoute(budgetId))
                },
                onEditBudget = { budgetId ->
                    navController.navigate(ScreenRoutes.EditBudget.createRoute(budgetId))
                }
            )
        }

        composable(
            route = ScreenRoutes.EditBudget.route,
            arguments = listOf(
                androidx.navigation.navArgument("budgetId") {
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
                    // We don't have the budgetId easily here unless we extract it from the ViewModel
                    // or pass it back. For now, let's just go back to Intro screen which will refresh.
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = ScreenRoutes.ViewBudget.route,
            arguments = listOf(
                androidx.navigation.navArgument("budgetId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) {
            val budgetId = it.arguments?.getString("budgetId") ?: ""
            ViewBudgetScreen(
                onEditBudget = {
                    navController.navigate(ScreenRoutes.EditBudget.createRoute(budgetId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
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

        composable(ScreenRoutes.Backup.route) {
            BackupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}