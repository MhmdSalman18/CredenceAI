package com.credenceai.app.domain.usecase


import com.credenceai.app.domain.repository.TransactionRepository
import javax.inject.Inject

class GetAllTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke() = repository.getAllTransactions()
}