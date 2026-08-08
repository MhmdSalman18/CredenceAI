package com.credenceai.app.presentation.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.core.preferences.PreferencesManager
import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.usecase.GetAllTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Transaction> = emptyList(),
    val currency: String = "INR (₹)"
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    getAllTransactionsUseCase: GetAllTransactionsUseCase,
    preferencesManager: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<NotificationsUiState> = combine(
        getAllTransactionsUseCase(),
        preferencesManager.currency
    ) { transactions, currency ->
        NotificationsUiState(
            notifications = transactions.sortedByDescending { it.dateTime },
            currency = currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationsUiState()
    )
}
