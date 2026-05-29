package com.credenceai.app.data.repository



import com.credenceai.app.data.local.dao.TransactionDao
import com.credenceai.app.data.mapper.toDomain
import com.credenceai.app.data.mapper.toEntity
import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override suspend fun addTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        dao.updateTransaction(transaction.toEntity())
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getUncategorizedTransactions(): Flow<List<Transaction>> {
        return dao.getUncategorizedTransactions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> {
        return dao.getTransactionsByDateRange(start, end).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun deleteAllTransactions() {
        dao.deleteAllTransactions()
    }
}