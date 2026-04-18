package com.credenceai.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object History : BottomNavItem("history", "History", Icons.Default.List)
    object Analytics : BottomNavItem("analytics", "Analytics", Icons.Default.AddCircle)
    object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}