package com.credenceai.app.presentation.ui.screens.smart_budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── Models ──────────────────────────────────────────────────────────────────

data class BudgetCategory(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val allocatedAmount: Double,
)

data class EditBudgetUiState(
    val totalBudget: String = "20,000",
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
    BudgetCategory(id = "food",      name = "Food",      iconEmoji = "🍴", allocatedAmount = 5_000.0),
    BudgetCategory(id = "transport", name = "Transport", iconEmoji = "🚗", allocatedAmount = 5_000.0),
    BudgetCategory(id = "bills",     name = "Bills",     iconEmoji = "🧾", allocatedAmount = 5_000.0),
    BudgetCategory(id = "shopping",  name = "Shopping",  iconEmoji = "🛍", allocatedAmount = 5_000.0),
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class EditBudgetViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditBudgetUiState())
    val uiState: StateFlow<EditBudgetUiState> = _uiState.asStateFlow()

    // ── Total Budget ─────────────────────────────────────────────────────────

    fun onTotalBudgetChanged(raw: String) {
        // Strip non-numeric chars except decimal
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
        val amount = rawAmount.replace(",", "").toDoubleOrNull() ?: return
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
                // TODO: inject & call your repository here
                // budgetRepository.saveBudget(uiState.value.toRequest())
                kotlinx.coroutines.delay(800) // Simulate network
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
            // Round to nearest 0.01
            kotlin.math.round(it * 100) / 100.0
        }
        return copy(
            categories = categories.map { it.copy(allocatedAmount = perCategory) }
        )
    }
}