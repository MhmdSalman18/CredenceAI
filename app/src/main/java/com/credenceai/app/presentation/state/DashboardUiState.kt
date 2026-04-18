package com.credenceai.app.presentation.state

import com.credenceai.app.domain.model.Transaction

data class DashboardUiState(

    val transactions: List<Transaction> = emptyList(),

    val totalIncome: Double = 0.0,

    val totalExpense: Double = 0.0,

    val balance: Double = 0.0
)