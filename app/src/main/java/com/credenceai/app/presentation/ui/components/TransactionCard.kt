package com.credenceai.app.presentation.ui.components



import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.credenceai.app.domain.model.Transaction

@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    Card {

        ListItem(
            headlineContent = {
                Text(transaction.merchant ?: "Unknown")
            },
            supportingContent = {
                Text("₹${transaction.amount}")
            },
            trailingContent = {
                Text(transaction.type)
            }
        )

        Button(onClick = onDelete) {
            Text("Delete")
        }
    }
}