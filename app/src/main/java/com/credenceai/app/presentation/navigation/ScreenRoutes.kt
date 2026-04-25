package com.credenceai.app.presentation.navigation

sealed class ScreenRoutes(val route: String) {
    object Home           : ScreenRoutes("home")
    object Dashboard      : ScreenRoutes("dashboard")
    object Transactions   : ScreenRoutes("transactions")
    object AddTransaction : ScreenRoutes("add_transaction")
    object AddExpense     : ScreenRoutes("add_expense")
    object AddIncome      : ScreenRoutes("add_income")
    object Uncategorized  : ScreenRoutes("uncategorized")
}