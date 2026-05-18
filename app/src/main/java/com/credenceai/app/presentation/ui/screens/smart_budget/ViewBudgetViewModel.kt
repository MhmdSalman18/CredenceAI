package com.credenceai.app.presentation.ui.screens.smart_budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.repository.BudgetRepository
import com.credenceai.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
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
    val isAddingSpend: Boolean = false,
    val showTransactionList: Boolean = false,
    val selectedCategoryForSpend: BudgetCategoryProgress? = null,
    val selectedCategoryTransactions: List<com.credenceai.app.domain.model.Transaction> = emptyList()
) {
    val spentAmount: Double get() = totalBudget - remainingBudget
    val usagePercentDisplay: String get() = "${"%.1f".format(usagePercent * 100)}%"
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class ViewBudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val budgetId: String = savedStateHandle["budgetId"] ?: ""

    private val _uiState = MutableStateFlow(ViewBudgetUiState())
    val uiState: StateFlow<ViewBudgetUiState> = _uiState.asStateFlow()

    private var allTransactions: List<com.credenceai.app.domain.model.Transaction> = emptyList()

    init {
        if (budgetId.isNotEmpty()) {
            loadBudget(budgetId)
        }
    }

    fun loadBudget(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                combine(
                    budgetRepository.getBudget(id),
                    transactionRepository.getAllTransactions()
                ) { budgetWithCats, transactions ->
                    allTransactions = transactions
                    if (budgetWithCats != null) {
                        val currentCalendar = Calendar.getInstance()
                        val currentMonth = currentCalendar.get(Calendar.MONTH)
                        val currentYear = currentCalendar.get(Calendar.YEAR)

                        // Filter transactions for the current month and non-income types
                        val monthlyExpenses = transactions.filter {
                            val cal = Calendar.getInstance().apply { timeInMillis = it.dateTime }
                            cal.get(Calendar.MONTH) == currentMonth &&
                                    cal.get(Calendar.YEAR) == currentYear &&
                                    it.type.lowercase() !in listOf("credit", "income")
                        }

                        val totalBudget = budgetWithCats.budget.totalBudget
                        val categories = budgetWithCats.categories.map { category ->
                            val spentInCategory = monthlyExpenses
                                .filter { it.category?.equals(category.name, ignoreCase = true) == true }
                                .sumOf { it.amount }

                            BudgetCategoryProgress(
                                id = category.id,
                                name = category.name,
                                iconEmoji = category.iconEmoji,
                                spentAmount = spentInCategory,
                                totalAmount = category.allocatedAmount
                            )
                        }

                        val spentTotal = categories.sumOf { it.spentAmount }
                        val remainingBudget = totalBudget - spentTotal
                        val usagePercent = if (totalBudget > 0) (spentTotal / totalBudget).toFloat() else 0f

                        ViewBudgetUiState(
                            isLoading = false,
                            budgetName = budgetWithCats.budget.name,
                            totalBudget = totalBudget,
                            remainingBudget = remainingBudget,
                            usagePercent = usagePercent,
                            categories = categories,
                            aiAdvice = generateAiAdvice(spentTotal, totalBudget, categories)
                        )
                    } else {
                        ViewBudgetUiState(isLoading = false)
                    }
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load budget")
                }
            }
        }
    }

    private fun generateAiAdvice(spent: Double, total: Double, categories: List<BudgetCategoryProgress>): String {
        if (total <= 0) return "Set up your budget to get AI-powered financial advice."
        
        val percent = (spent / total)
        val overBudget = categories.filter { it.spentAmount > it.totalAmount }
        
        return when {
            overBudget.isNotEmpty() -> {
                "You've exceeded your budget in ${overBudget.size} categories: ${overBudget.joinToString { it.name }}. Consider reducing spending elsewhere."
            }
            percent > 0.9 -> {
                "You've used ${"%.0f".format(percent * 100)}% of your total budget. Be careful with your spending for the rest of the month."
            }
            percent > 0.5 -> {
                "You're halfway through your budget. You're doing okay, but keep an eye on your categories."
            }
            spent > 0 -> {
                "Great start! You're well within your budget. Keep tracking your expenses."
            }
            else -> {
                "You haven't recorded any expenses for this budget yet. Start tracking to see your progress!"
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onAddSpendClick(category: BudgetCategoryProgress) {
        _uiState.update { it.copy(isAddingSpend = true, selectedCategoryForSpend = category) }
    }

    fun onCategoryClick(category: BudgetCategoryProgress) {
        val currentCalendar = Calendar.getInstance()
        val currentMonth = currentCalendar.get(Calendar.MONTH)
        val currentYear = currentCalendar.get(Calendar.YEAR)

        val transactions = allTransactions.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateTime }
            cal.get(Calendar.MONTH) == currentMonth &&
                    cal.get(Calendar.YEAR) == currentYear &&
                    it.category?.equals(category.name, ignoreCase = true) == true &&
                    it.type.lowercase() !in listOf("credit", "income")
        }
        
        _uiState.update { 
            it.copy(
                showTransactionList = true, 
                selectedCategoryForSpend = category,
                selectedCategoryTransactions = transactions
            ) 
        }
    }

    fun onDismissTransactionList() {
        _uiState.update { it.copy(showTransactionList = false, selectedCategoryForSpend = null, selectedCategoryTransactions = emptyList()) }
    }

    fun onDismissAddSpend() {
        _uiState.update { it.copy(isAddingSpend = false, selectedCategoryForSpend = null) }
    }

    fun onDeleteTransaction(transaction: com.credenceai.app.domain.model.Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
                // The combine in loadBudget will automatically update the UI
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete transaction: ${e.message}") }
            }
        }
    }

    fun onSaveSpend(amount: Double, note: String) {
        val category = _uiState.value.selectedCategoryForSpend ?: return
        viewModelScope.launch {
            try {
                val transaction = com.credenceai.app.domain.model.Transaction(
                    amount = amount,
                    type = "debit",
                    merchant = category.name,
                    dateTime = System.currentTimeMillis(),
                    category = category.name,
                    source = "BUDGET",
                    note = note,
                    paymentMode = "CASH",
                    referenceId = "budget_manual_${System.currentTimeMillis()}"
                )
                transactionRepository.addTransaction(transaction)
                onDismissAddSpend()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to save spend: ${e.message}") }
            }
        }
    }
}
