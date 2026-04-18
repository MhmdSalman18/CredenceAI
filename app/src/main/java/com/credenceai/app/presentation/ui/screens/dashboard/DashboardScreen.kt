package com.credenceai.app.presentation.ui.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column {

        Text("Balance: ₹${state.balance}")

        Button(onClick = onNavigateToAdd) {
            Text("Add Transaction")
        }

        Button(onClick = onNavigateToTransactions) {
            Text("View Transactions")
        }
    }
}