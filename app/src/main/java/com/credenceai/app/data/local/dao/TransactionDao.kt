package com.credenceai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.credenceai.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY dateTime DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category IS NULL")
    fun getUncategorizedTransactions(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE dateTime BETWEEN :start AND :end
    """)
    fun getTransactionsByDateRange(
        start: Long,
        end: Long
    ): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}