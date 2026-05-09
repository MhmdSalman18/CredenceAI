package com.credenceai.app.presentation.ui.screens.smart_budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val budgetName: String = "My Budget",
    val totalBudget: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val usagePercent: Float = 0f,
    val categories: List<BudgetCategoryProgress> = emptyList(),
    val aiAdvice: String = "Set up your budget to get AI-powered financial advice.",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val spentAmount: Double get() = totalBudget - remainingBudget
    val usagePercentDisplay: String get() = "${"%.1f".format(usagePercent * 100)}%"
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class ViewBudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val budgetId: String = savedStateHandle["budgetId"] ?: ""

    private val _uiState = MutableStateFlow(ViewBudgetUiState())
    val uiState: StateFlow<ViewBudgetUiState> = _uiState.asStateFlow()

    init {
        if (budgetId.isNotEmpty()) {
            loadBudget(budgetId)
        }
    }

    fun loadBudget(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                budgetRepository.getBudget(id).collect { budgetWithCats ->
                    if (budgetWithCats != null) {
                        val totalBudget = budgetWithCats.budget.totalBudget
                        val categories = budgetWithCats.categories.map {
                            // In a real app, spentAmount would come from TransactionRepository
                            BudgetCategoryProgress(
                                id = it.id,
                                name = it.name,
                                iconEmoji = it.iconEmoji,
                                spentAmount = 0.0, // TODO: Link with transactions
                                totalAmount = it.allocatedAmount
                            )
                        }
                        val spentTotal = categories.sumOf { it.spentAmount }
                        val remainingBudget = totalBudget - spentTotal
                        val usagePercent = if (totalBudget > 0) (spentTotal / totalBudget).toFloat() else 0f

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                budgetName = budgetWithCats.budget.name,
                                totalBudget = totalBudget,
                                remainingBudget = remainingBudget,
                                usagePercent = usagePercent,
                                categories = categories,
                                aiAdvice = "You have set a budget of ₹${"%.0f".format(totalBudget)}. Track your expenses to see AI advice here."
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
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
