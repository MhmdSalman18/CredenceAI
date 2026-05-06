package com.credenceai.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.credenceai.app.data.local.dao.BudgetDao
import com.credenceai.app.data.local.dao.TransactionDao
import com.credenceai.app.data.local.entity.BudgetCategoryEntity
import com.credenceai.app.data.local.entity.BudgetEntity
import com.credenceai.app.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        BudgetCategoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CredenceDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
}