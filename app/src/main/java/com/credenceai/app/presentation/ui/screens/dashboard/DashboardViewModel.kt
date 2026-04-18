package com.credenceai.app.presentation.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.usecase.GetAllTransactionsUseCase
import com.credenceai.app.presentation.state.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getAllTransactions: GetAllTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            getAllTransactions().collect { list ->

                val income = list
                    .filter { it.type == "credit" }
                    .sumOf { it.amount }

                val expense = list
                    .filter { it.type == "debit" }
                    .sumOf { it.amount }

                _state.value = DashboardUiState(
                    transactions = list,
                    totalIncome = income,
                    totalExpense = expense,
                    balance = income - expense
                )
            }
        }
    }
}