package com.credenceai.app.presentation.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.usecase.GetAllTransactionsUseCase
import com.credenceai.app.domain.usecase.GetUncategorizedTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

// ─── Models ───────────────────────────────────────────────────────────────────

data class SpendingPoint(
    val label: String,   // e.g. "OCT 01"
    val value: Float     // normalized 0..1 for chart drawing
)

data class CategorySlice(
    val name: String,
    val percent: Float,       // 0..1
    val displayPercent: String, // "45%"
    val color: Long           // ARGB hex
)

data class MerchantItem(
    val name: String,
    val amount: String,
    val barPercent: Float,    // 0..1 relative to max
    val iconLabel: String     // single char / emoji for placeholder icon
)

// ─── UI State ─────────────────────────────────────────────────────────────────

data class AnalyticsUiState(
    val selectedPeriod: String          = "Current Month",
    val uncategorizedCount: Int         = 0,
    val showUncategorizedBanner: Boolean= false,

    // Summary
    val income: String                  = "₹0.00",
    val incomeChange: String            = "0% vs last month",
    val incomePositive: Boolean         = true,
    val expenses: String                = "₹0.00",
    val expensesChange: String          = "0% vs last month",
    val expensesPositive: Boolean       = false,
    val savings: String                 = "₹0.00",
    val savingsRate: String             = "0% savings rate",

    // Insight
    val insightTitle: String            = "No insights yet",
    val insightBody: String             = "Start adding transactions to see your financial patterns.",
    val insightAction: String           = "Add Transaction",

    // Spending trend chart
    val spendingPoints: List<SpendingPoint> = emptyList(),

    // Category breakdown
    val totalSpendLabel: String         = "₹0.00",
    val categorySlices: List<CategorySlice> = emptyList(),

    // Top merchants
    val topMerchants: List<MerchantItem> = emptyList()
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val getUncategorizedTransactionsUseCase: GetUncategorizedTransactionsUseCase
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow("Current Month")
    private val _showUncategorizedBanner = MutableStateFlow(true)

    val uiState: StateFlow<AnalyticsUiState> = combine(
        getAllTransactionsUseCase(),
        getUncategorizedTransactionsUseCase(),
        _selectedPeriod,
        _showUncategorizedBanner
    ) { allTransactions, uncategorized, period, showBanner ->
        
        // Filter out uncategorized transactions from the main totals
        val transactions = allTransactions.filter { it.category != null }

        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        val monthTransactions = transactions.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateTime }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }

        val totalIncome = monthTransactions.filter { it.type.lowercase() == "credit" || it.type.lowercase() == "income" }.sumOf { it.amount }
        val totalSpent = monthTransactions.filter { it.type.lowercase() == "debit" || it.type.lowercase() == "expense" }.sumOf { it.amount }
        val savings = totalIncome - totalSpent
        val savingsRate = if (totalIncome > 0) (savings / totalIncome * 100).toInt() else 0

        // Category breakdown
        val expenseTransactions = monthTransactions.filter { it.type.lowercase() == "debit" || it.type.lowercase() == "expense" }
        val categoryGroups = expenseTransactions.groupBy { it.category ?: "OTHERS" }
        val slices = categoryGroups.map { (cat, txs) ->
            val amt = txs.sumOf { it.amount }
            val pct = if (totalSpent > 0) (amt / totalSpent).toFloat() else 0f
            CategorySlice(
                name = cat,
                percent = pct,
                displayPercent = "${(pct * 100).toInt()}%",
                color = getColorForCategory(cat)
            )
        }.sortedByDescending { it.percent }

        // Top merchants
        val merchants = expenseTransactions.groupBy { it.merchant ?: "Unknown" }
            .map { (name, txs) ->
                val amt = txs.sumOf { it.amount }
                name to amt
            }.sortedByDescending { it.second }
            .take(3)
        
        val maxMerchantAmt = merchants.firstOrNull()?.second ?: 1.0
        val merchantItems = merchants.map { (name, amt) ->
            MerchantItem(
                name = name,
                amount = "₹%.2f".format(Locale.getDefault(), amt),
                barPercent = (amt / maxMerchantAmt).toFloat(),
                iconLabel = name.take(1).uppercase()
            )
        }

        // Spending points (simplified: group by week)
        val points = (0..4).map { week ->
            val weekAmt = expenseTransactions.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateTime }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                day in (week * 7 + 1)..(week * 7 + 7)
            }.sumOf { it.amount }
            SpendingPoint("W${week + 1}", if (totalSpent > 0) (weekAmt / totalSpent).toFloat() else 0f)
        }

        val topCategory = slices.firstOrNull()?.name ?: "N/A"

        AnalyticsUiState(
            selectedPeriod = period,
            uncategorizedCount = uncategorized.size,
            showUncategorizedBanner = showBanner && (uncategorized.isNotEmpty() || true), // Force show for now as requested
            income = "₹%.2f".format(Locale.getDefault(), totalIncome),
            expenses = "₹%.2f".format(Locale.getDefault(), totalSpent),
            savings = "₹%.2f".format(Locale.getDefault(), savings),
            savingsRate = "$savingsRate% savings rate",
            totalSpendLabel = "₹%.1fk".format(Locale.getDefault(), totalSpent / 1000),
            categorySlices = slices,
            topMerchants = merchantItems,
            spendingPoints = points,
            insightTitle = if (topCategory != "N/A") "$topCategory is your highest expense" else "No major expenses yet",
            insightBody = if (topCategory != "N/A") "You've spent ₹%.2f on $topCategory this month.".format(Locale.getDefault(), categoryGroups[topCategory]?.sumOf { it.amount } ?: 0.0) else "Keep tracking to see insights.",
            insightAction = "Analyze $topCategory"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    fun onDismissUncategorizedBanner() {
        _showUncategorizedBanner.value = false
    }

    fun onReviewUncategorized() {
        // TODO: navigate to uncategorized screen
    }

    private fun getColorForCategory(category: String): Long {
        return when (category.uppercase()) {
            "FOOD", "FOOD & DINING" -> 0xFFE07B39
            "SHOPPING" -> 0xFF7B5EA7
            "TRAVEL" -> 0xFF2D9CDB
            "BILLS", "BILLS & UTILITIES" -> 0xFFE05252
            "TRANSFER" -> 0xFF27AE60
            else -> 0xFF8A94A6
        }
    }

    fun onPeriodChange(period: String) {
        _selectedPeriod.value = period
    }

    fun onAnalyzeInsight() {
        // TODO: navigate to category deep-dive
    }

    fun onViewAllMerchants() {
        // TODO: navigate to merchants list
    }
}
