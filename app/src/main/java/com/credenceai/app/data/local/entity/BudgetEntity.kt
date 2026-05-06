package com.credenceai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String = "monthly_budget", // Simple case: only one active monthly budget
    val totalBudget: Double,
    val autoDistribute: Boolean,
    val repeatEveryMonth: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)
