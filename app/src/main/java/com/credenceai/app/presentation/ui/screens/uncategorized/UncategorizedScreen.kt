package com.credenceai.app.presentation.ui.screens.uncategorized


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.credenceai.app.domain.model.Transaction
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val categories = listOf(
    "Food", "Shopping", "Travel", "Bills",
    "Recharge", "Entertainment", "Medical",
    "Education", "Transfer", "Others"
)

@Composable
fun UncategorizedScreen(
    viewModel: UncategorizedViewModel = hiltViewModel()
) {

    val transactions by viewModel.transactions.collectAsState()

    LazyColumn {

        items(transactions) { transaction ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {

                Column(modifier = Modifier.padding(12.dp)) {

                    Text(transaction.merchant ?: "Unknown")
                    Text("₹${transaction.amount}")

                    Spacer(modifier = Modifier.height(8.dp))

                    categories.forEach { category ->

                        Button(
                            onClick = {
                                viewModel.updateCategory(transaction, category)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}