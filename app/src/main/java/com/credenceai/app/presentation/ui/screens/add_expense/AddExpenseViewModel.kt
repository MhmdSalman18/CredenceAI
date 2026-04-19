package com.credenceai.app.presentation.ui.screens.add_expense

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.URI

data class AddExpenseUiState(
    val amount: String = "",
    val merchantName: String = "",
    val category: String = "",
    val paymentMode: String = "UPI",
    val dateTime: String = "",
    val notes: String = "",
    val receiptUri: URI? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
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

class AddExpenseViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun onAmountChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(amount = value, errorMessage = null) }
        }
    }

    fun onMerchantNameChange(value: String) {
        _uiState.update { it.copy(merchantName = value) }
    }

    fun onCategoryChange(value: String) {
        _uiState.update { it.copy(category = value) }
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
        // TODO: Inject and call repository to save transaction
        _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}