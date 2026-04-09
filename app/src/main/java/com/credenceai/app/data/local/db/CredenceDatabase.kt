package com.credenceai.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.credenceai.app.data.local.dao.TransactionDao
import com.credenceai.app.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CredenceDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
}