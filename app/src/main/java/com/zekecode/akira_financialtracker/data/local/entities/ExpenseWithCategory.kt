package com.zekecode.akira_financialtracker.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseWithCategory(
    @Embedded val expense: ExpenseModel,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryModel
)
