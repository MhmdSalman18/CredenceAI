package com.credenceai.app.data.repository

import com.credenceai.app.data.local.dao.BudgetDao
import com.credenceai.app.data.local.entity.BudgetCategoryEntity
import com.credenceai.app.data.local.entity.BudgetEntity
import com.credenceai.app.data.local.entity.BudgetWithCategories
import com.credenceai.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getAllBudgets(): Flow<List<BudgetWithCategories>> {
        return budgetDao.getAllBudgetsWithCategories()
    }

    override fun getBudget(budgetId: String): Flow<BudgetWithCategories?> {
        return budgetDao.getBudgetWithCategories(budgetId)
    }

    override suspend fun saveBudget(budget: BudgetEntity, categories: List<BudgetCategoryEntity>) {
        budgetDao.saveBudget(budget, categories)
    }
}
