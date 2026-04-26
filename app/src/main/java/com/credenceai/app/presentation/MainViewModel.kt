package com.credenceai.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.usecase.GetUncategorizedTransactionsUseCase
import com.credenceai.app.presentation.navigation.ScreenRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUncategorizedTransactionsUseCase: GetUncategorizedTransactionsUseCase
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            val uncategorized = getUncategorizedTransactionsUseCase().first()
            android.util.Log.d("MainViewModel", "Found ${uncategorized.size} uncategorized transactions")
            if (uncategorized.isNotEmpty()) {
                android.util.Log.d("MainViewModel", "Setting start destination to Uncategorized")
                _startDestination.value = ScreenRoutes.Uncategorized.route
            } else {
                android.util.Log.d("MainViewModel", "Setting start destination to Home")
                _startDestination.value = ScreenRoutes.Home.route
            }
        }
    }
}