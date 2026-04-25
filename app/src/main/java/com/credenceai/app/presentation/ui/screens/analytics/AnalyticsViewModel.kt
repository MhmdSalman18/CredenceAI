package com.credenceai.app.presentation.ui.screens.analytics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    val selectedPeriod: String          = "October 2023",
    val uncategorizedCount: Int         = 5,
    val showUncategorizedBanner: Boolean= true,

    // Summary
    val income: String                  = "\$12,450.00",
    val incomeChange: String            = "+12% vs last month",
    val incomePositive: Boolean         = true,
    val expenses: String                = "\$6,842.15",
    val expensesChange: String          = "-4% vs last month",
    val expensesPositive: Boolean       = false,
    val savings: String                 = "\$5,607.85",
    val savingsRate: String             = "45% savings rate",

    // Insight
    val insightTitle: String            = "Food is your highest expense this month",
    val insightBody: String             = "You've spent \$1,240 on dining out, which is 18% higher than your 6-month average.",
    val insightAction: String           = "Analyze Dining",

    // Spending trend chart
    val spendingPoints: List<SpendingPoint> = defaultSpendingPoints(),

    // Category breakdown
    val totalSpendLabel: String         = "\$6.8k",
    val categorySlices: List<CategorySlice> = defaultSlices(),

    // Top merchants
    val topMerchants: List<MerchantItem> = defaultMerchants()
)

private fun defaultSpendingPoints() = listOf(
    SpendingPoint("OCT 01", 0.15f),
    SpendingPoint("OCT 08", 0.35f),
    SpendingPoint("OCT 15", 0.55f),
    SpendingPoint("OCT 22", 0.90f),
    SpendingPoint("OCT 31", 0.60f)
)

private fun defaultSlices() = listOf(
    CategorySlice("Housing",   0.45f, "45%", 0xFF2D5BE3),
    CategorySlice("Dining",    0.25f, "25%", 0xFF27AE60),
    CategorySlice("Transport", 0.15f, "15%", 0xFFE05252),
    CategorySlice("Other",     0.15f, "15%", 0xFFCDD0DA)
)

private fun defaultMerchants() = listOf(
    MerchantItem("Whole Foods Market",      "\$1,245.90", 1.00f, "W"),
    MerchantItem("Tesla Supercharger",      "\$420.00",   0.34f, "⚡"),
    MerchantItem("Digital Services Bundle", "\$189.50",   0.15f, "D")
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class AnalyticsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun onDismissUncategorizedBanner() {
        _uiState.update { it.copy(showUncategorizedBanner = false) }
    }

    fun onReviewUncategorized() {
        // TODO: navigate to uncategorized screen
    }

    fun onPeriodChange(period: String) {
        _uiState.update { it.copy(selectedPeriod = period) }
        // TODO: reload data for selected period
    }

    fun onAnalyzeInsight() {
        // TODO: navigate to category deep-dive
    }

    fun onViewAllMerchants() {
        // TODO: navigate to merchants list
    }
}