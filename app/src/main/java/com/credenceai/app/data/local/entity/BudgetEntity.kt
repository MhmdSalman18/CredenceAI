package com.credenceai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "My Budget",
    val totalBudget: Double,
    val autoDistribute: Boolean,
    val repeatEveryMonth: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)
