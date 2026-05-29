package com.credenceai.app.domain.repository


import com.credenceai.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun addTransaction(transaction: Transaction)

    suspend fun deleteTransaction(transaction: Transaction)

    suspend fun updateTransaction(transaction: Transaction)

    fun getAllTransactions(): Flow<List<Transaction>>

    fun getUncategorizedTransactions(): Flow<List<Transaction>>

    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>>

    suspend fun deleteAllTransactions()
}