package com.credenceai.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_categories",
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("budgetId")]
)
data class BudgetCategoryEntity(
    @PrimaryKey
    val id: String,
    val budgetId: String,
    val name: String,
    val iconEmoji: String,
    val allocatedAmount: Double
)
