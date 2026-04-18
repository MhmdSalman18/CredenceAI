package com.credenceai.app.presentation.navigation

sealed class ScreenRoutes(val route: String) {

    object Dashboard : ScreenRoutes("dashboard")
    object Transactions : ScreenRoutes("transactions")
    object AddTransaction : ScreenRoutes("add_transaction")
    object Uncategorized : ScreenRoutes("uncategorized")
}