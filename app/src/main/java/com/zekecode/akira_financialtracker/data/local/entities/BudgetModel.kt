package com.zekecode.akira_financialtracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetModel(
    @PrimaryKey
    val yearMonth: String, // Format: "YYYY-MM"
    val amount: Double
)
