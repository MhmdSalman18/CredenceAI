package com.credenceai.app.presentation.navigation

sealed class ScreenRoutes(val route: String) {
    object Home           : ScreenRoutes("home")
    object AddExpense     : ScreenRoutes("add_expense")
    object AddIncome      : ScreenRoutes("add_income")
    object Uncategorized  : ScreenRoutes("uncategorized")
    object SmartBudget    : ScreenRoutes("smart_budget")
    object EditBudget     : ScreenRoutes("edit_budget/{budgetId}") {
        fun createRoute(budgetId: String = "new") = "edit_budget/$budgetId"
    }
    object ViewBudget     : ScreenRoutes("view_budget/{budgetId}") {
        fun createRoute(budgetId: String) = "view_budget/$budgetId"
    }
    object SupportedBanks : ScreenRoutes("supported_banks")
    object Notifications  : ScreenRoutes("notifications")
    object Backup         : ScreenRoutes("backup")
}
