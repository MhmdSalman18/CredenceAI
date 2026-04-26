package com.credenceai.app.presentation.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.usecase.GetAllTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import java.util.Locale

// ─── Models ───────────────────────────────────────────────────────────────────

data class CategoryItem(
    val id: String,
    val name: String,
    val amount: String,
    val spendPercent: String,
    val icon: ImageVector,
    val iconTint: Color
)

// ─── UI State ─────────────────────────────────────────────────────────────────

data class HomeUiState(
    val netBalance: String             = "₹0.00",
    val changeLabel: String            = "No changes this month",
    val totalIncome: String            = "₹0.00",
    val totalSpent: String             = "₹0.00",
    val budgetProgress: Float          = 0.0f,
    val categories: List<CategoryItem> = emptyList(),
    val isLoading: Boolean             = false,
    val errorMessage: String?          = null
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getAllTransactionsUseCase()
        .map { allTransactions ->
            // Filter out uncategorized transactions (captured from notifications but not yet saved/reviewed)
            val transactions = allTransactions.filter { it.category != null }

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
                    amount = "₹%.2f".format(Locale.getDefault(), categoryAmount),
                    spendPercent = "%.1f%% of spend".format(Locale.getDefault(), percent),
                    icon = getIconForCategory(categoryName),
                    iconTint = getColorForCategory(categoryName)
                )
            }

            HomeUiState(
                netBalance = "₹%.2f".format(Locale.getDefault(), netBalance),
                totalIncome = "₹%.2f".format(Locale.getDefault(), totalIncome),
                totalSpent = "₹%.2f".format(Locale.getDefault(), totalSpent),
                budgetProgress = if (totalIncome > 0) (totalSpent / totalIncome).toFloat().coerceIn(0f, 1f) else 0f,
                categories = categories.sortedByDescending { it.amount.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0 },
                isLoading = false
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
            "FOOD", "FOOD & DINING" -> Color(0xFFE07B39)
            "SHOPPING" -> Color(0xFF7B5EA7)
            "TRAVEL" -> Color(0xFF2D9CDB)
            "BILLS", "BILLS & UTILITIES" -> Color(0xFFE05252)
            else -> Color(0xFF8A94A6)
        }
    }

    // ── User actions ──────────────────────────────────────────────────────────

    fun onAddIncome() {
        // TODO: navigate to Add Income screen
    }

    fun onExportReport() {
        // TODO: trigger PDF / CSV export
    }

    fun onViewAllCategories() {
        // TODO: navigate to Categories screen
    }

    fun dismissError() {
    }
}
