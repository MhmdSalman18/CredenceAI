package com.credenceai.app.presentation.ui.screens.transactions


import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.credenceai.app.presentation.ui.components.TransactionCard

@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()

    LazyColumn {
        items(transactions) { transaction ->

            TransactionCard(
                transaction = transaction,
                onDelete = {
                    viewModel.deleteTransaction(transaction)
                }
            )
        }
    }
}