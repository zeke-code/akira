package com.zekecode.akira_financialtracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zekecode.akira_financialtracker.data.local.dao.CategoryDao
import com.zekecode.akira_financialtracker.data.local.dao.ExpenseDao
import com.zekecode.akira_financialtracker.data.local.dao.EarningDao
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [CategoryModel::class, ExpenseModel::class, EarningModel::class], version = 2)
abstract class AkiraDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
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
                    .addCallback(AkiraDatabaseCallback(scope)) // Add the callback here
                    .fallbackToDestructiveMigration() // or addMigrations(MIGRATION_X_Y) if using migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AkiraDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(database.categoryDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao) {

            // Add default categories
            val categories = listOf(
                // Revenue Categories
                CategoryModel(name = "Salary", icon = "ic_question_mark"),
                CategoryModel(name = "Freelancing", icon = "ic_question_mark"),
                CategoryModel(name = "Investment", icon = "ic_question_mark"),
                CategoryModel(name = "Gift", icon = "ic_question_mark"),
                CategoryModel(name = "Bonus", icon = "ic_question_mark"),
                CategoryModel(name = "Interest", icon = "ic_question_mark"),
                CategoryModel(name = "Rental Income", icon = "ic_question_mark"),
                CategoryModel(name = "Dividends", icon = "ic_question_mark"),
                CategoryModel(name = "Refund", icon = "ic_question_mark"),
                CategoryModel(name = "Tax Return", icon = "ic_question_mark"),

                // Expense Categories
                CategoryModel(name = "Groceries", icon = "ic_question_mark"),
                CategoryModel(name = "Rent", icon = "ic_question_mark"),
                CategoryModel(name = "Utilities", icon = "ic_question_mark"),
                CategoryModel(name = "Transportation", icon = "ic_question_mark"),
                CategoryModel(name = "Healthcare", icon = "ic_question_mark"),
                CategoryModel(name = "Education", icon = "ic_question_mark"),
                CategoryModel(name = "Dining Out", icon = "ic_question_mark"),
                CategoryModel(name = "Entertainment", icon = "ic_question_mark"),
                CategoryModel(name = "Insurance", icon = "ic_question_mark"),
                CategoryModel(name = "Clothing", icon = "ic_question_mark"),
                CategoryModel(name = "Travel", icon = "ic_question_mark"),
                CategoryModel(name = "Subscriptions", icon = "ic_question_mark"),
                CategoryModel(name = "Household Supplies", icon = "ic_question_mark"),
                CategoryModel(name = "Charity", icon = "ic_question_mark"),
                CategoryModel(name = "Pet Care", icon = "ic_question_mark"),
                CategoryModel(name = "Miscellaneous", icon = "ic_question_mark")
            )

            // Insert categories into the database
            categories.forEach { category ->
                categoryDao.insertCategory(category)
            }
        }

    }
}
