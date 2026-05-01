package com.credenceai.app.presentation.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.usecase.GetAllTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    getAllTransactionsUseCase: GetAllTransactionsUseCase
) : ViewModel() {

    // For now, we use transactions as "notifications" since they are generated from SMS/Notifications
    val notifications: StateFlow<List<Transaction>> = getAllTransactionsUseCase()
        .map { transactions ->
            transactions.sortedByDescending { it.dateTime }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
