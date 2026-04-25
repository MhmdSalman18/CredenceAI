package com.credenceai.app.presentation.ui.screens.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    val isCredit: Boolean,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color
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

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val allGroups = listOf(
        TransactionGroup(
            dateLabel = "TODAY",
            transactions = listOf(
                TransactionItem(
                    id              = "1",
                    merchantName    = "Unknown Merchant 042",
                    category        = "UNCATEGORIZED",
                    isUncategorized = true,
                    time            = "10:42 AM",
                    amount          = "- ₹42.50",
                    isCredit        = false,
                    icon            = Icons.Default.QuestionMark,
                    iconTint        = Color(0xFFE05252),
                    iconBackground  = Color(0xFFFDECEC)
                ),
                TransactionItem(
                    id             = "2",
                    merchantName   = "Apple Store",
                    category       = "SHOPPING",
                    time           = "09:15 AM",
                    amount         = "- ₹1,299.00",
                    isCredit       = false,
                    icon           = Icons.Default.ShoppingBag,
                    iconTint       = Color(0xFF2D5BE3),
                    iconBackground = Color(0xFFE8EEFB)
                )
            )
        ),
        TransactionGroup(
            dateLabel = "YESTERDAY",
            transactions = listOf(
                TransactionItem(
                    id             = "3",
                    merchantName   = "Salary Deposit",
                    category       = "TRANSFER",
                    time           = "4:00 PM",
                    amount         = "+ ₹4,500.00",
                    isCredit       = true,
                    icon           = Icons.Default.AccountBalance,
                    iconTint       = Color(0xFF27AE60),
                    iconBackground = Color(0xFFE8F8EF)
                ),
                TransactionItem(
                    id             = "4",
                    merchantName   = "Delta Airlines",
                    category       = "TRAVEL",
                    time           = "1:20 PM",
                    amount         = "- ₹340.00",
                    isCredit       = false,
                    icon           = Icons.Default.Flight,
                    iconTint       = Color(0xFF2D9CDB),
                    iconBackground = Color(0xFFE7F5FC)
                )
            )
        ),
        TransactionGroup(
            dateLabel = "APR 20",
            transactions = listOf(
                TransactionItem(
                    id             = "5",
                    merchantName   = "Zomato",
                    category       = "FOOD",
                    time           = "8:30 PM",
                    amount         = "- ₹650.00",
                    isCredit       = false,
                    icon           = Icons.Default.Restaurant,
                    iconTint       = Color(0xFFE07B39),
                    iconBackground = Color(0xFFFAF0E8)
                ),
                TransactionItem(
                    id             = "6",
                    merchantName   = "Netflix",
                    category       = "ENTERTAINMENT",
                    time           = "12:00 PM",
                    amount         = "- ₹499.00",
                    isCredit       = false,
                    icon           = Icons.Default.Tv,
                    iconTint       = Color(0xFF7B5EA7),
                    iconBackground = Color(0xFFF0EBF8)
                )
            )
        )
    )

    init {
        _uiState.update { it.copy(groups = allGroups, filteredGroups = allGroups) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onFilterChange(filter: TransactionFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
        applyFilters()
    }

    fun onAddTransactionClick() {
        // TODO: navigate to AddExpense
    }

    private fun applyFilters() {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        val filter = state.activeFilter

        val result = allGroups.mapNotNull { group ->
            val filtered = group.transactions.filter { tx ->
                val matchesQuery = query.isEmpty() ||
                        tx.merchantName.lowercase().contains(query) ||
                        tx.category.lowercase().contains(query)
                val matchesFilter = when (filter) {
                    TransactionFilter.ALL      -> true
                    TransactionFilter.DEBIT    -> !tx.isCredit
                    TransactionFilter.CREDIT   -> tx.isCredit
                    TransactionFilter.FOOD     -> tx.category == "FOOD"
                    TransactionFilter.SHOPPING -> tx.category == "SHOPPING"
                    TransactionFilter.TRAVEL   -> tx.category == "TRAVEL"
                    TransactionFilter.TRANSFER -> tx.category == "TRANSFER"
                }
                matchesQuery && matchesFilter
            }
            if (filtered.isEmpty()) null else group.copy(transactions = filtered)
        }

        _uiState.update { it.copy(filteredGroups = result) }
    }
}