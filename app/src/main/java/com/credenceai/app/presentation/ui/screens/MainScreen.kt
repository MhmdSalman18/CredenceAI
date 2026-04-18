package com.credenceai.app.presentation.ui.screens

import android.R.attr.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.credenceai.app.presentation.navigation.*
import com.credenceai.app.presentation.ui.screens.dashboard.DashboardScreen
import com.credenceai.app.presentation.ui.screens.transactions.TransactionsScreen
import com.credenceai.app.presentation.ui.screens.addedit.AddEditTransactionScreen
import com.credenceai.app.presentation.ui.screens.home.HomeScreen
import com.credenceai.app.presentation.ui.screens.uncategorized.UncategorizedScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Analytics,
        BottomNavItem.Settings
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
            startDestination = ScreenRoutes.Home.route,
            modifier = Modifier.padding(padding)
        ) {


            composable("history") { TransactionsScreen() }
            composable("analytics") { TransactionsScreen() }
            composable("settings") { TransactionsScreen() }

            composable(ScreenRoutes.Transactions.route) {
                TransactionsScreen()
            }

            composable(ScreenRoutes.AddTransaction.route) {
                AddEditTransactionScreen()
            }
            composable(ScreenRoutes.Uncategorized.route) {
                UncategorizedScreen()
            }
            composable(ScreenRoutes.Home.route){
                HomeScreen()
            }
        }
    }
}