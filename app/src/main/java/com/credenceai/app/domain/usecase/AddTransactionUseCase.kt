package com.credenceai.app.domain.usecase

import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.addTransaction(transaction)
    }
}