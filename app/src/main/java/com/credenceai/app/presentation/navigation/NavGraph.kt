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
                    navController.navigate(ScreenRoutes.EditBudget.route)
                }
            )
        }

        composable(ScreenRoutes.EditBudget.route) {
            EditBudgetScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onBudgetSaved = {
                    navController.navigate(ScreenRoutes.ViewBudget.route) {
                        popUpTo(ScreenRoutes.SmartBudget.route) { inclusive = true }
                    }
                }
            )
        }

        composable(ScreenRoutes.ViewBudget.route) {
            ViewBudgetScreen(
                onEditBudget = {
                    navController.navigate(ScreenRoutes.EditBudget.route)
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
    }
}