package com.credenceai.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Dashboard", Icons.Default.Dashboard)
    object History : BottomNavItem("history", "History", Icons.Default.History)
    object Analytics : BottomNavItem("analytics", "Analytics", Icons.Default.AutoGraph)
    object Settings : BottomNavItem("settings", "Settings", Icons.AutoMirrored.Filled.List)
}