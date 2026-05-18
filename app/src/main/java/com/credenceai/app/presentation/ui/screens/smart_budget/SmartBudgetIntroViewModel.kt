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

data class ExistingBudgetSummary(
    val id: String,
    val name: String,
    val totalBudget: Double,
    val categoryCount: Int,
)

data class SmartBudgetIntroUiState(
    val existingBudgets: List<ExistingBudgetSummary> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class SmartBudgetIntroViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartBudgetIntroUiState())
    val uiState: StateFlow<SmartBudgetIntroUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            budgetRepository.getAllBudgets().collect { budgetList ->
                val budgets = budgetList.map { budgetWithCats ->
                    ExistingBudgetSummary(
                        id = budgetWithCats.budget.id,
                        name = budgetWithCats.budget.name,
                        totalBudget = budgetWithCats.budget.totalBudget,
                        categoryCount = budgetWithCats.categories.size,
                    )
                }
                _uiState.update {
                    it.copy(existingBudgets = budgets, isLoading = false)
                }
            }
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budgetId)
        }
    }
}
