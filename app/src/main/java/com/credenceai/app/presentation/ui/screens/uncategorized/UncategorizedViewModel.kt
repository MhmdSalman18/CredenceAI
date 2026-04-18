package com.credenceai.app.presentation.ui.screens.uncategorized


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.usecase.GetUncategorizedTransactionsUseCase
import com.credenceai.app.domain.usecase.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UncategorizedViewModel @Inject constructor(
    private val getUncategorized: GetUncategorizedTransactionsUseCase,
    private val updateTransaction: UpdateTransactionUseCase
) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> =
        getUncategorized()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun updateCategory(transaction: Transaction, category: String) {
        viewModelScope.launch {
            val updated = transaction.copy(category = category)
            updateTransaction(updated)
        }
    }
}