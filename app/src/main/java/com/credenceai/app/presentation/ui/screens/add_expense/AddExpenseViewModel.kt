package com.credenceai.app.presentation.ui.screens.add_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URI
import javax.inject.Inject

data class AddExpenseUiState(
    val amount: String = "",
    val merchantName: String = "",
    val category: String = "",
    val paymentMode: String = "UPI",
    val dateTime: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val receiptUri: URI? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val transactionType: String = "debit"
)

val categories = listOf(
    "Food & Dining",
    "Shopping",
    "Transport",
    "Entertainment",
    "Health",
    "Bills & Utilities",
    "Travel",
    "Education",
    "Income",
    "Others"
)

val paymentModes = listOf(
    "UPI",
    "Credit Card",
    "Debit Card",
    "Net Banking",
    "Cash",
    "Wallet"
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        updateDateTimeString(_uiState.value.timestamp)
    }

    private fun updateDateTimeString(timestamp: Long) {
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault())
        val dateString = sdf.format(java.util.Date(timestamp))
        _uiState.update { it.copy(dateTime = dateString, timestamp = timestamp) }
    }

    fun onDateSelected(millis: Long) {
        val currentCalendar = java.util.Calendar.getInstance()
        currentCalendar.timeInMillis = _uiState.value.timestamp
        
        val selectedCalendar = java.util.Calendar.getInstance()
        selectedCalendar.timeInMillis = millis
        
        currentCalendar.set(java.util.Calendar.YEAR, selectedCalendar.get(java.util.Calendar.YEAR))
        currentCalendar.set(java.util.Calendar.MONTH, selectedCalendar.get(java.util.Calendar.MONTH))
        currentCalendar.set(java.util.Calendar.DAY_OF_MONTH, selectedCalendar.get(java.util.Calendar.DAY_OF_MONTH))
        
        updateDateTimeString(currentCalendar.timeInMillis)
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = _uiState.value.timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
        calendar.set(java.util.Calendar.MINUTE, minute)
        updateDateTimeString(calendar.timeInMillis)
    }

    fun setTransactionType(type: String) {
        _uiState.update { it.copy(transactionType = type) }
    }

    fun onAmountChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(amount = value, errorMessage = null) }
        }
    }

    fun onMerchantNameChange(value: String) {
        _uiState.update { it.copy(merchantName = value) }
    }

    fun onCategoryChange(value: String) {
        _uiState.update { 
            it.copy(
                category = value,
                transactionType = if (value.lowercase() == "income") "credit" else "debit"
            )
        }
    }

    fun onPaymentModeChange(value: String) {
        _uiState.update { it.copy(paymentMode = value) }
    }

    fun onDateTimeChange(value: String) {
        _uiState.update { it.copy(dateTime = value) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun fillFromTransaction(amount: Double, merchant: String, timestamp: Long, type: String) {
        _uiState.update {
            it.copy(
                amount = "%.2f".format(java.util.Locale.getDefault(), amount),
                merchantName = merchant,
                timestamp = timestamp,
                transactionType = type.lowercase()
            )
        }
        updateDateTimeString(timestamp)
    }

    fun onReceiptSelected(uri: URI?) {
        _uiState.update { it.copy(receiptUri = uri) }
    }

    fun onSaveTransaction() {
        val state = _uiState.value
        if (state.amount.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter an amount") }
            return
        }
        if (state.merchantName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter merchant name") }
            return
        }
        if (state.category.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select a category") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    amount = state.amount.toDoubleOrNull() ?: 0.0,
                    type = state.transactionType,
                    merchant = state.merchantName,
                    dateTime = state.timestamp,
                    category = state.category,
                    source = "MANUAL",
                    note = state.notes,
                    paymentMode = state.paymentMode,
                    referenceId = null
                )
                addTransactionUseCase(transaction)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Failed to save transaction") }
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
