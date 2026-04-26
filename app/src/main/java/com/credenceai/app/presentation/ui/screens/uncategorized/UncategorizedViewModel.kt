package com.credenceai.app.presentation.ui.screens.uncategorized

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.usecase.DeleteTransactionUseCase
import com.credenceai.app.domain.usecase.GetUncategorizedTransactionsUseCase
import com.credenceai.app.domain.usecase.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class Transaction(
    val id: String,
    val merchantName: String,
    val subLabel: String,
    val amount: Double,
    val isCredit: Boolean,
    val date: String,
    val time: String,
    val rawTimestamp: Long,
    val type: String,
    val iconType: TransactionIconType,
    val category: TransactionCategory? = null
)

enum class TransactionIconType {
    SHOPPING, TRANSPORT, ENTERTAINMENT, UTILITY, OTHER
}

enum class TransactionCategory {
    FOOD, SHOPPING, TRAVEL, BILLS, ENTERTAINMENT, OTHER
}

data class UncategorizedUiState(
    val transactions: List<Transaction> = emptyList(),
    val isAutoSyncActive: Boolean = true
)

@HiltViewModel
class UncategorizedViewModel @Inject constructor(
    private val getUncategorizedTransactionsUseCase: GetUncategorizedTransactionsUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    val uiState: StateFlow<UncategorizedUiState> = getUncategorizedTransactionsUseCase().map { transactions ->
        UncategorizedUiState(
            transactions = transactions.map { domainTx ->
                val id = domainTx.id.toString()
                val isCredit = domainTx.type.lowercase() == "credit" || domainTx.type.lowercase() == "income"
                Transaction(
                    id = id,
                    merchantName = domainTx.merchant ?: "Unknown",
                    subLabel = domainTx.referenceId ?: "No Reference ID",
                    amount = domainTx.amount,
                    isCredit = isCredit,
                    date = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(domainTx.dateTime)).uppercase(),
                    time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(domainTx.dateTime)),
                    rawTimestamp = domainTx.dateTime,
                    type = domainTx.type,
                    iconType = getIconType(domainTx.category),
                    category = null
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UncategorizedUiState()
    )

    private fun getIconType(category: String?): TransactionIconType {
        return when (category?.uppercase()) {
            "SHOPPING" -> TransactionIconType.SHOPPING
            "TRANSPORT", "TRAVEL", "TRAIN" -> TransactionIconType.TRANSPORT
            "ENTERTAINMENT", "MOVIES" -> TransactionIconType.ENTERTAINMENT
            "UTILITY", "BILLS" -> TransactionIconType.UTILITY
            else -> TransactionIconType.OTHER
        }
    }

    fun setCategory(transactionId: String, category: TransactionCategory) {
        viewModelScope.launch {
            val transactions = getUncategorizedTransactionsUseCase().first()
            val domainTx = transactions.find { it.id.toString() == transactionId }
            domainTx?.let {
                updateTransactionUseCase(it.copy(category = category.name))
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            val transactions = getUncategorizedTransactionsUseCase().first()
            val domainTx = transactions.find { it.id.toString() == transactionId }
            domainTx?.let {
                deleteTransactionUseCase(it)
            }
        }
    }
}