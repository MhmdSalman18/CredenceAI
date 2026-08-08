package com.credenceai.app.presentation.ui.screens.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.credenceai.app.ui.theme.*
import com.credenceai.app.core.preferences.PreferencesManager
import com.credenceai.app.core.utils.CurrencyUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.usecase.GetAllTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ─── Models ───────────────────────────────────────────────────────────────────

enum class TransactionFilter(val label: String) {
    ALL("All"),
    DEBIT("Debit"),
    CREDIT("Credit"),
    FOOD("Food"),
    SHOPPING("Shopping"),
    TRAVEL("Travel"),
    TRANSFER("Transfer")
}

data class TransactionItem(
    val id: String,
    val merchantName: String,
    val category: String,
    val isUncategorized: Boolean = false,
    val time: String,
    val amount: String,
    val rawAmount: Double,
    val isCredit: Boolean,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val timestamp: Long = 0L
)

data class TransactionGroup(
    val dateLabel: String,   // e.g. "TODAY", "YESTERDAY", "APR 20"
    val transactions: List<TransactionItem>
)

// ─── UI State ─────────────────────────────────────────────────────────────────

data class HistoryUiState(
    val searchQuery: String = "",
    val activeFilter: TransactionFilter = TransactionFilter.ALL,
    val groups: List<TransactionGroup> = emptyList(),
    val filteredGroups: List<TransactionGroup> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(TransactionFilter.ALL)

    val uiState: StateFlow<HistoryUiState> = combine(
        getAllTransactionsUseCase(),
        _searchQuery,
        _activeFilter,
        preferencesManager.currency
    ) { transactions, query, filter, currency ->
        val filteredTransactions = transactions.filter { tx ->
            val matchesQuery = query.isEmpty() ||
                    tx.merchant?.lowercase()?.contains(query.lowercase()) == true ||
                    tx.category?.lowercase()?.contains(query.lowercase()) == true
            
            val matchesFilter = when (filter) {
                TransactionFilter.ALL      -> true
                TransactionFilter.DEBIT    -> tx.type.lowercase() == "debit"
                TransactionFilter.CREDIT   -> tx.type.lowercase() == "credit"
                TransactionFilter.FOOD     -> tx.category?.uppercase() == "FOOD" || tx.category?.uppercase() == "FOOD & DINING"
                TransactionFilter.SHOPPING -> tx.category?.uppercase() == "SHOPPING"
                TransactionFilter.TRAVEL   -> tx.category?.uppercase() == "TRAVEL"
                TransactionFilter.TRANSFER -> tx.category?.uppercase() == "TRANSFER"
            }
            matchesQuery && matchesFilter
        }

        val groups = filteredTransactions
            .sortedByDescending { it.dateTime }
            .groupBy { formatDate(it.dateTime) }
            .map { (date, txs) ->
                TransactionGroup(
                    dateLabel = date,
                    transactions = txs.map { it.toUiItem(currency) }
                )
            }

        HistoryUiState(
            searchQuery = query,
            activeFilter = filter,
            filteredGroups = groups,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: TransactionFilter) {
        _activeFilter.value = filter
    }

    private fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        
        calendar.timeInMillis = timestamp
        
        return when {
            isSameDay(calendar, today) -> "TODAY"
            isSameDay(calendar, yesterday) -> "YESTERDAY"
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(calendar.time).uppercase()
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun com.credenceai.app.domain.model.Transaction.toUiItem(currency: String): TransactionItem {
        val isCredit = type.lowercase() == "credit" || type.lowercase() == "income"
        val categoryName = category ?: "UNCATEGORIZED"
        return TransactionItem(
            id = id.toString(),
            merchantName = merchant ?: "Unknown",
            category = categoryName.uppercase(),
            isUncategorized = category == null || category.isEmpty(),
            time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dateTime)),
            amount = "${if (isCredit) "+" else "-"} ${CurrencyUtils.formatAmount(amount, currency)}",
            rawAmount = amount,
            isCredit = isCredit,
            icon = getIconForCategory(categoryName),
            iconTint = getColorForCategory(categoryName),
            iconBackground = getColorForCategory(categoryName).copy(alpha = 0.1f),
            timestamp = dateTime
        )
    }

    private fun getIconForCategory(category: String): ImageVector {
        return when (category.uppercase()) {
            "FOOD", "FOOD & DINING" -> Icons.Default.Restaurant
            "SHOPPING" -> Icons.Default.ShoppingBag
            "TRAVEL" -> Icons.Default.Flight
            "BILLS", "BILLS & UTILITIES" -> Icons.Default.Receipt
            "TRANSFER" -> Icons.Default.AccountBalance
            else -> if (category.isEmpty()) Icons.Default.QuestionMark else Icons.Default.Receipt
        }
    }

    private fun getColorForCategory(category: String): Color {
        return when (category.uppercase()) {
            "FOOD", "FOOD & DINING" -> CategoryFood
            "SHOPPING" -> CategoryShopping
            "TRAVEL" -> CategoryTravel
            "BILLS", "BILLS & UTILITIES" -> DebitRed
            "TRANSFER" -> CreditGreen
            else -> CategoryDefault
        }
    }
}
