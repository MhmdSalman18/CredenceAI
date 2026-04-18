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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val netBalance: String        = "\$12,450.80",
    val changeLabel: String       = "+2.4% from last month",
    val totalIncome: String       = "\$18,200.00",
    val totalSpent: String        = "\$5,749.20",
    val budgetProgress: Float     = 0.68f,
    val categories: List<CategoryItem> = defaultCategories(),
    val isLoading: Boolean        = false,
    val errorMessage: String?     = null
)

private fun defaultCategories() = listOf(
    CategoryItem(
        id           = "food",
        name         = "FOOD",
        amount       = "\$1,240",
        spendPercent = "21.5% of spend",
        icon         = Icons.Default.Restaurant,
        iconTint     = Color(0xFFE07B39)
    ),
    CategoryItem(
        id           = "shopping",
        name         = "SHOPPING",
        amount       = "\$890",
        spendPercent = "15.4% of spend",
        icon         = Icons.Default.ShoppingBag,
        iconTint     = Color(0xFF7B5EA7)
    ),
    CategoryItem(
        id           = "travel",
        name         = "TRAVEL",
        amount       = "\$2,100",
        spendPercent = "36.5% of spend",
        icon         = Icons.Default.Flight,
        iconTint     = Color(0xFF2D9CDB)
    ),
    CategoryItem(
        id           = "bills",
        name         = "BILLS",
        amount       = "\$1,519",
        spendPercent = "26.6% of spend",
        icon         = Icons.Default.Receipt,
        iconTint     = Color(0xFFE05252)
    )
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private fun loadSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // TODO: replace with real repository calls
                // val summary    = repository.getMonthlySummary()
                // val categories = repository.getCategoryBreakdown()
                // _uiState.update { it.copy(
                //     netBalance    = summary.netBalance.formatCurrency(),
                //     changeLabel   = summary.changeLabel,
                //     totalIncome   = summary.income.formatCurrency(),
                //     totalSpent    = summary.spent.formatCurrency(),
                //     budgetProgress= summary.budgetUsedFraction,
                //     categories    = categories.toUiItems(),
                //     isLoading     = false
                // ) }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // ── User actions ──────────────────────────────────────────────────────────

    fun onAddExpense() {
        // TODO: navigate to Add Expense screen or show bottom sheet
    }

    fun onAddIncome() {
        // TODO: navigate to Add Income screen or show bottom sheet
    }

    fun onExportReport() {
        // TODO: trigger PDF / CSV export
    }

    fun onViewAllCategories() {
        // TODO: navigate to Categories screen
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}