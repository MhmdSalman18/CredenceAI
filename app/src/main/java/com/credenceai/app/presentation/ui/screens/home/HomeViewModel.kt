package com.credenceai.app.presentation.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.credenceai.app.ui.theme.*
import com.credenceai.app.core.utils.CurrencyUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.core.preferences.PreferencesManager
import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.usecase.GetAllTransactionsUseCase
import com.credenceai.app.domain.usecase.ExportTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Locale
import java.util.Calendar

// ─── Models ───────────────────────────────────────────────────────────────────

data class CategoryItem(
    val id: String,
    val name: String,
    val amount: String,
    val spendPercent: String,
    val icon: ImageVector,
    val iconTint: Color
)

sealed class HomeEffect {
    data class ExportReport(val csvData: String) : HomeEffect()
}

// ─── UI State ─────────────────────────────────────────────────────────────────

data class HomeUiState(
    val userName: String               = "User",
    val netBalance: String             = "$0.00",
    val changeLabel: String            = "No changes this month",
    val totalIncome: String            = "$0.00",
    val totalSpent: String             = "$0.00",
    val budgetProgress: Float          = 0.0f,
    val categories: List<CategoryItem> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val uncategorizedCount: Int        = 0,
    val isLoading: Boolean             = false,
    val errorMessage: String?          = null,
    val currency: String               = "INR (₹)"
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val getUncategorizedTransactionsUseCase: com.credenceai.app.domain.usecase.GetUncategorizedTransactionsUseCase,
    private val exportTransactionsUseCase: ExportTransactionsUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        getAllTransactionsUseCase(),
        getUncategorizedTransactionsUseCase(),
        preferencesManager.userName,
        preferencesManager.currency
    ) { allTransactions, uncategorized, name, currency ->
            // Filter out uncategorized transactions and Budget specific transactions for Home Screen summary
            val transactions = allTransactions.filter { it.category != null && it.source != "BUDGET" }

            val totalIncome = transactions.filter { it.type.lowercase() == "credit" || it.type.lowercase() == "income" }.sumOf { it.amount }
            val totalSpent = transactions.filter { it.type.lowercase() == "debit" || it.type.lowercase() == "expense" }.sumOf { it.amount }
            val netBalance = totalIncome - totalSpent
            
            val categoryGroups = transactions
                .filter { it.type.lowercase() == "debit" || it.type.lowercase() == "expense" }
                .groupBy { it.category ?: "OTHERS" }
            
            val categories = categoryGroups.map { (categoryName, categoryTransactions) ->
                val categoryAmount = categoryTransactions.sumOf { it.amount }
                val percent = if (totalSpent > 0) (categoryAmount / totalSpent * 100) else 0.0
                CategoryItem(
                    id = categoryName.lowercase(),
                    name = categoryName.uppercase(),
                    amount = CurrencyUtils.formatAmount(categoryAmount, currency),
                    spendPercent = "%.1f%% of spend".format(Locale.getDefault(), percent),
                    icon = getIconForCategory(categoryName),
                    iconTint = getColorForCategory(categoryName)
                )
            }

            val recent = transactions.sortedByDescending { it.dateTime }.take(5)

            HomeUiState(
                userName = name.split(" ").firstOrNull() ?: "User",
                netBalance = CurrencyUtils.formatAmount(netBalance, currency),
                totalIncome = CurrencyUtils.formatAmount(totalIncome, currency),
                totalSpent = CurrencyUtils.formatAmount(totalSpent, currency),
                budgetProgress = if (totalIncome > 0) (totalSpent / totalIncome).toFloat().coerceIn(0f, 1f) else 0f,
                categories = categories.sortedByDescending { it.amount.replace(CurrencyUtils.extractSymbol(currency), "").replace(",", "").toDoubleOrNull() ?: 0.0 },
                recentTransactions = recent,
                uncategorizedCount = uncategorized.size,
                isLoading = false,
                currency = currency
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )

    private fun getIconForCategory(category: String): ImageVector {
        return when (category.uppercase()) {
            "FOOD", "FOOD & DINING" -> Icons.Default.Restaurant
            "SHOPPING" -> Icons.Default.ShoppingBag
            "TRAVEL" -> Icons.Default.Flight
            "BILLS", "BILLS & UTILITIES" -> Icons.Default.Receipt
            else -> Icons.Default.Receipt
        }
    }

    private fun getColorForCategory(category: String): Color {
        return when (category.uppercase()) {
            "FOOD", "FOOD & DINING" -> CategoryFood
            "SHOPPING" -> CategoryShopping
            "TRAVEL" -> CategoryTravel
            "BILLS", "BILLS & UTILITIES" -> DebitRed
            else -> CategoryDefault
        }
    }

    // ── User actions ──────────────────────────────────────────────────────────

    fun onAddIncome() {
        // TODO: navigate to Add Income screen
    }

    fun onExportReport() {
        viewModelScope.launch {
            val csvData = exportTransactionsUseCase()
            _effect.emit(HomeEffect.ExportReport(csvData))
        }
    }

    fun onViewAllCategories() {
        // TODO: navigate to Categories screen
    }

    fun dismissError() {
    }
}
