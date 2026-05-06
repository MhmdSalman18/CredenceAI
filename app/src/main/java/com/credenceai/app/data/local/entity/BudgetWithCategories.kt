package com.credenceai.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BudgetWithCategories(
    @Embedded val budget: BudgetEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "budgetId"
    )
    val categories: List<BudgetCategoryEntity>
)
