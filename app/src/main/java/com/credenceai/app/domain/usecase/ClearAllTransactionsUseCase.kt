package com.credenceai.app.domain.usecase

import com.credenceai.app.domain.repository.TransactionRepository
import javax.inject.Inject

class ClearAllTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke() {
        repository.deleteAllTransactions()
    }
}
