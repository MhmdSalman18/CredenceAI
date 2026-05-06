package com.credenceai.app.di

import android.content.Context
import androidx.room.Room
import com.credenceai.app.data.local.dao.BudgetDao
import com.credenceai.app.data.local.dao.TransactionDao
import com.credenceai.app.data.local.db.CredenceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CredenceDatabase {
        return Room.databaseBuilder(
            context,
            CredenceDatabase::class.java,
            "credence_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTransactionDao(
        db: CredenceDatabase
    ): TransactionDao {
        return db.transactionDao()
    }

    @Provides
    fun provideBudgetDao(
        db: CredenceDatabase
    ): BudgetDao {
        return db.budgetDao()
    }
}