package com.credenceai.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem(
        route = "dashboard",
        label = "Home",
        icon = Icons.Default.Home
    )

    object Transactions : BottomNavItem(
        route = "transactions",
        label = "Transactions",
        icon = Icons.Default.List
    )

    object Add : BottomNavItem(
        route = "add_transaction",
        label = "Add",
        icon = Icons.Default.Add
    )
}