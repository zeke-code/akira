package com.zekecode.akira_financialtracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zekecode.akira_financialtracker.data.local.dao.EarningDao
import com.zekecode.akira_financialtracker.data.local.dao.ExpenseDao
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel

@Database(entities = [EarningModel::class, ExpenseModel::class], version = 1)
abstract class AkiraDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun earningDao(): EarningDao
}