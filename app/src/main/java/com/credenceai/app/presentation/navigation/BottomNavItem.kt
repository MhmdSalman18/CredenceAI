package com.credenceai.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Dashboard", Icons.Rounded.Dashboard)
    object History : BottomNavItem("history", "History", Icons.Rounded.History)
    object Analytics : BottomNavItem("analytics", "Analytics", Icons.Rounded.Insights)
    object Settings : BottomNavItem("settings", "Settings", Icons.Rounded.Settings)
}