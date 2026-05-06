package com.credenceai.app.presentation.ui.screens.smart_budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── Models ───────────────────────────────────────────────────────────────────

enum class SpendingStatus { GOOD, WARNING, EXCEEDED }

data class BudgetCategoryProgress(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val spentAmount: Double,
    val totalAmount: Double,
) {
    val remainingAmount: Double get() = totalAmount - spentAmount
    val usagePercent: Float get() = if (totalAmount == 0.0) 0f else (spentAmount / totalAmount).toFloat().coerceIn(0f, 1f)
    val status: SpendingStatus get() = when {
        spentAmount > totalAmount -> SpendingStatus.EXCEEDED
        usagePercent >= 0.80f    -> SpendingStatus.WARNING
        else                     -> SpendingStatus.GOOD
    }
    val spentLabel: String get() = "Spent ₹${"%.0f".format(spentAmount)} of ₹${"%.0f".format(totalAmount)}"
    val remainingLabel: String get() = when (status) {
        SpendingStatus.EXCEEDED  -> "-₹${"%.0f".format(kotlin.math.abs(remainingAmount))}"
        else                     -> "₹${"%.0f".format(remainingAmount)} left"
    }
}

data class ViewBudgetUiState(
    val totalBudget: Double = 20_000.0,
    val remainingBudget: Double = 15_500.0,
    val usagePercent: Float = 0.225f,
    val categories: List<BudgetCategoryProgress> = defaultCategories(),
    val aiAdvice: String = "Based on current trends, you might exceed your Bills budget by 15K next month.",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val spentAmount: Double get() = totalBudget - remainingBudget
    val usagePercentDisplay: String get() = "${"%.1f".format(usagePercent * 100)}%"
}

private fun defaultCategories() = listOf(
    BudgetCategoryProgress(
        id = "food", name = "Food", iconEmoji = "🍴",
        spentAmount = 500.0, totalAmount = 5_000.0
    ),
    BudgetCategoryProgress(
        id = "transport", name = "Transport", iconEmoji = "🚗",
        spentAmount = 4_000.0, totalAmount = 5_000.0
    ),
    BudgetCategoryProgress(
        id = "bills", name = "Bills", iconEmoji = "🧾",
        spentAmount = 6_000.0, totalAmount = 5_000.0
    ),
    BudgetCategoryProgress(
        id = "shopping", name = "Shopping", iconEmoji = "🛍",
        spentAmount = 1_000.0, totalAmount = 5_000.0
    ),
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class ViewBudgetViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ViewBudgetUiState())
    val uiState: StateFlow<ViewBudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudget()
    }

    fun loadBudget() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // TODO: val budget = budgetRepository.getCurrentBudget()
                // _uiState.update { it.copy(isLoading = false, ...mapFromDomain(budget)) }
                kotlinx.coroutines.delay(600) // Simulate network
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load budget")
                }
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}