package com.credenceai.app.presentation.ui.screens.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    fun addTransaction(
        amount: Double,
        type: String,
        merchant: String,
        category: String?,
        paymentMode: String?
    ) {
        viewModelScope.launch {

            val transaction = Transaction(
                amount = amount,
                type = type,
                merchant = merchant,
                dateTime = System.currentTimeMillis(),
                category = category,
                source = "MANUAL",
                note = null,
                paymentMode = paymentMode,
                referenceId = null
            )

            addTransactionUseCase(transaction)
        }
    }
}