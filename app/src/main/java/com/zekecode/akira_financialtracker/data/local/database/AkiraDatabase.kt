package com.zekecode.akira_financialtracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zekecode.akira_financialtracker.data.local.dao.EarningDao
import com.zekecode.akira_financialtracker.data.local.dao.ExpenseDao
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel
import kotlinx.coroutines.CoroutineScope

@Database(entities = [EarningModel::class, ExpenseModel::class], version = 1)
abstract class AkiraDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun earningDao(): EarningDao

    companion object {
        @Volatile
        private var INSTANCE: AkiraDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AkiraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AkiraDatabase::class.java,
                    "akira_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}