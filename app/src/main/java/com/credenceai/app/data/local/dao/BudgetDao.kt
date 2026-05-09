package com.credenceai.app.data.local.dao

import androidx.room.*
import com.credenceai.app.data.local.entity.BudgetCategoryEntity
import com.credenceai.app.data.local.entity.BudgetEntity
import com.credenceai.app.data.local.entity.BudgetWithCategories
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<BudgetCategoryEntity>)

    @Transaction
    @Query("SELECT * FROM budgets")
    fun getAllBudgetsWithCategories(): Flow<List<BudgetWithCategories>>

    @Transaction
    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    fun getBudgetWithCategories(budgetId: String): Flow<BudgetWithCategories?>

    @Transaction
    suspend fun saveBudget(budget: BudgetEntity, categories: List<BudgetCategoryEntity>) {
        insertBudget(budget)
        // Clear old categories for this budget if needed, but since we are using REPLACE on ID, 
        // if we add/remove categories we might need to handle it.
        // For simplicity in this implementation, we can delete existing categories for the budget first.
        deleteCategoriesForBudget(budget.id)
        insertCategories(categories)
    }

    @Query("DELETE FROM budget_categories WHERE budgetId = :budgetId")
    suspend fun deleteCategoriesForBudget(budgetId: String)
}
