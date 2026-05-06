package com.credenceai.app.presentation.ui.screens.smart_budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.data.local.entity.BudgetCategoryEntity
import com.credenceai.app.data.local.entity.BudgetEntity
import com.credenceai.app.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Models ──────────────────────────────────────────────────────────────────

data class BudgetCategory(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val allocatedAmount: Double,
)

data class EditBudgetUiState(
    val totalBudget: String = "20000",
    val autoDistribute: Boolean = true,
    val repeatEveryMonth: Boolean = true,
    val categories: List<BudgetCategory> = defaultCategories(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
) {
    val assignedAmount: Double get() = categories.sumOf { it.allocatedAmount }
    val remainingAmount: Double get() = (totalBudgetAsDouble) - assignedAmount
    val totalBudgetAsDouble: Double get() =
        totalBudget.replace(",", "").toDoubleOrNull() ?: 0.0
}

private fun defaultCategories() = listOf(
    BudgetCategory(id = "food",      name = "Food",      iconEmoji = "🍴", allocatedAmount = 5000.0),
    BudgetCategory(id = "transport", name = "Transport", iconEmoji = "🚗", allocatedAmount = 5000.0),
    BudgetCategory(id = "bills",     name = "Bills",     iconEmoji = "🧾", allocatedAmount = 5000.0),
    BudgetCategory(id = "shopping",  name = "Shopping",  iconEmoji = "🛍", allocatedAmount = 5000.0),
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class EditBudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditBudgetUiState())
    val uiState: StateFlow<EditBudgetUiState> = _uiState.asStateFlow()

    init {
        // Load existing budget if any
        viewModelScope.launch {
            budgetRepository.getBudget("monthly_budget").collect { budgetWithCats ->
                budgetWithCats?.let { data ->
                    _uiState.update { state ->
                        state.copy(
                            totalBudget = data.budget.totalBudget.toInt().toString(),
                            autoDistribute = data.budget.autoDistribute,
                            repeatEveryMonth = data.budget.repeatEveryMonth,
                            categories = data.categories.map {
                                BudgetCategory(it.id, it.name, it.iconEmoji, it.allocatedAmount)
                            }
                        )
                    }
                }
            }
        }
    }

    // ── Total Budget ─────────────────────────────────────────────────────────

    fun onTotalBudgetChanged(raw: String) {
        val cleaned = raw.filter { it.isDigit() || it == '.' }
        _uiState.update { state ->
            val updated = state.copy(totalBudget = cleaned)
            if (state.autoDistribute) updated.withAutoDistributed() else updated
        }
    }

    // ── Toggles ──────────────────────────────────────────────────────────────

    fun onAutoDistributeToggled(enabled: Boolean) {
        _uiState.update { state ->
            val updated = state.copy(autoDistribute = enabled)
            if (enabled) updated.withAutoDistributed() else updated
        }
    }

    fun onRepeatEveryMonthToggled(enabled: Boolean) {
        _uiState.update { it.copy(repeatEveryMonth = enabled) }
    }

    // ── Category Allocation ──────────────────────────────────────────────────

    fun onCategoryAmountChanged(categoryId: String, rawAmount: String) {
        val amount = rawAmount.replace(",", "").toDoubleOrNull() ?: 0.0
        _uiState.update { state ->
            state.copy(
                categories = state.categories.map { cat ->
                    if (cat.id == categoryId) cat.copy(allocatedAmount = amount) else cat
                }
            )
        }
    }

    fun onAddCategory(name: String, iconEmoji: String = "📂") {
        val newCategory = BudgetCategory(
            id = name.lowercase().replace(" ", "_"),
            name = name,
            iconEmoji = iconEmoji,
            allocatedAmount = 0.0,
        )
        _uiState.update { state ->
            val updated = state.copy(categories = state.categories + newCategory)
            if (state.autoDistribute) updated.withAutoDistributed() else updated
        }
    }

    fun onRemoveCategory(categoryId: String) {
        _uiState.update { state ->
            val updated = state.copy(
                categories = state.categories.filter { it.id != categoryId }
            )
            if (state.autoDistribute) updated.withAutoDistributed() else updated
        }
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    fun onSaveBudget() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val currentState = _uiState.value
                val budget = BudgetEntity(
                    totalBudget = currentState.totalBudgetAsDouble,
                    autoDistribute = currentState.autoDistribute,
                    repeatEveryMonth = currentState.repeatEveryMonth
                )
                val categories = currentState.categories.map {
                    BudgetCategoryEntity(
                        id = it.id,
                        budgetId = "monthly_budget",
                        name = it.name,
                        iconEmoji = it.iconEmoji,
                        allocatedAmount = it.allocatedAmount
                    )
                }
                budgetRepository.saveBudget(budget, categories)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to save budget")
                }
            }
        }
    }

    fun onSaveHandled() {
        _uiState.update { it.copy(isSaved = false) }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun EditBudgetUiState.withAutoDistributed(): EditBudgetUiState {
        val count = categories.size
        if (count == 0) return this
        val perCategory = (totalBudgetAsDouble / count).let {
            kotlin.math.round(it * 100) / 100.0
        }
        return copy(
            categories = categories.map { it.copy(allocatedAmount = perCategory) }
        )
    }
}
