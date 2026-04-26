package com.credenceai.app.presentation.ui.screens.uncategorized

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class Transaction(
    val id: String,
    val merchantName: String,
    val subLabel: String,
    val amount: Double,
    val date: String,
    val time: String,
    val iconType: TransactionIconType,
    val category: TransactionCategory? = null
)

enum class TransactionIconType {
    SHOPPING, TRANSPORT, ENTERTAINMENT, UTILITY
}

enum class TransactionCategory {
    FOOD, TRANSPORT, ENTERTAINMENT, BILLS, SHOPPING, OTHER
}

data class UncategorizedUiState(
    val transactions: List<Transaction> = emptyList(),
    val isAutoSyncActive: Boolean = true,
    val isSaving: Boolean = false
)

@HiltViewModel
class UncategorizedViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        UncategorizedUiState(
            transactions = listOf(
                Transaction(
                    id = "1",
                    merchantName = "Blue Tokai Coffee",
                    subLabel = "UPI ID: paytm-482910@oksbi",
                    amount = -12.50,
                    date = "OCT 24",
                    time = "09:15 AM",
                    iconType = TransactionIconType.SHOPPING
                ),
                Transaction(
                    id = "2",
                    merchantName = "Uber India Systems",
                    subLabel = "UPI ID: uber.pay@icici",
                    amount = -24.80,
                    date = "OCT 23",
                    time = "06:42 PM",
                    iconType = TransactionIconType.TRANSPORT
                ),
                Transaction(
                    id = "3",
                    merchantName = "Netflix Entertainment",
                    subLabel = "Merchant ID: NETFLX-USA-901",
                    amount = -19.99,
                    date = "OCT 22",
                    time = "11:00 AM",
                    iconType = TransactionIconType.ENTERTAINMENT
                ),
                Transaction(
                    id = "4",
                    merchantName = "TATA Power DDL",
                    subLabel = "UPI ID: tatapower@axisbank",
                    amount = -142.15,
                    date = "OCT 21",
                    time = "04:20 PM",
                    iconType = TransactionIconType.UTILITY
                )
            )
        )
    )
    val uiState: StateFlow<UncategorizedUiState> = _uiState.asStateFlow()

    fun setCategory(transactionId: String, category: TransactionCategory) {
        _uiState.value = _uiState.value.copy(
            transactions = _uiState.value.transactions.map { transaction ->
                if (transaction.id == transactionId) transaction.copy(category = category)
                else transaction
            }
        )
    }

    fun saveCategorizedTransactions(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isSaving = true)
        // TODO: Wire up repository call here
        onSuccess()
    }

    fun skipForNow(onSkip: () -> Unit) {
        onSkip()
    }
}