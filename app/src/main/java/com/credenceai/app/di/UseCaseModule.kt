package com.credenceai.app.di


import com.credenceai.app.data.repository.TransactionRepositoryImpl
import com.credenceai.app.domain.repository.TransactionRepository
import com.credenceai.app.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideRepository(
        repoImpl: TransactionRepositoryImpl
    ): TransactionRepository = repoImpl

    @Provides
    fun provideAddTransactionUseCase(
        repository: TransactionRepository
    ) = AddTransactionUseCase(repository)

    @Provides
    fun provideGetAllTransactionsUseCase(
        repository: TransactionRepository
    ) = GetAllTransactionsUseCase(repository)

    @Provides
    fun provideDeleteTransactionUseCase(
        repository: TransactionRepository
    ) = DeleteTransactionUseCase(repository)

    @Provides
    fun provideUpdateTransactionUseCase(
        repository: TransactionRepository
    ) = UpdateTransactionUseCase(repository)
}