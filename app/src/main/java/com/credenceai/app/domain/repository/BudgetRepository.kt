package com.credenceai.app.domain.repository

import com.credenceai.app.data.local.entity.BudgetCategoryEntity
import com.credenceai.app.data.local.entity.BudgetEntity
import com.credenceai.app.data.local.entity.BudgetWithCategories
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAllBudgets(): Flow<List<BudgetWithCategories>>
    fun getBudget(budgetId: String): Flow<BudgetWithCategories?>
    suspend fun saveBudget(budget: BudgetEntity, categories: List<BudgetCategoryEntity>)
}
