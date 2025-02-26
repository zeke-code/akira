package com.zekecode.akira_financialtracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "earnings")
data class EarningModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val categoryId: Int,
    // Date is Long type because Room doesn't support Date conversion.
    // We base ourselves on Unix Epochs.
    val date: Long,
    val description: String? = null,
)
