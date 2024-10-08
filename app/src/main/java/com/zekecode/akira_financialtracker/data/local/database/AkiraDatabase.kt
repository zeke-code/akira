package com.zekecode.akira_financialtracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.dao.BudgetDao
import com.zekecode.akira_financialtracker.data.local.dao.CategoryDao
import com.zekecode.akira_financialtracker.data.local.dao.ExpenseDao
import com.zekecode.akira_financialtracker.data.local.dao.EarningDao
import com.zekecode.akira_financialtracker.data.local.entities.BudgetModel
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [CategoryModel::class, ExpenseModel::class, EarningModel::class, BudgetModel::class], version = 3, exportSchema = false)
abstract class AkiraDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun earningDao(): EarningDao
    abstract fun budgetDao(): BudgetDao

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
                    .addCallback(AkiraDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
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
                CategoryModel(name = "Salary", icon = R.drawable.ic_dollar),
                CategoryModel(name = "Freelancing", icon = R.drawable.ic_work),
                CategoryModel(name = "Investment", icon = R.drawable.ic_stocks),
                CategoryModel(name = "Gift", icon = R.drawable.ic_gift),
                CategoryModel(name = "Rental Income", icon = R.drawable.ic_house),
                CategoryModel(name = "Dividends", icon = R.drawable.ic_dollar_outlined),
                CategoryModel(name = "Refund", icon = R.drawable.ic_dollar_outlined),

                // Expense Categories
                CategoryModel(name = "Groceries", icon = R.drawable.ic_grocery_cart),
                CategoryModel(name = "Rent", icon = R.drawable.ic_money_crossed),
                CategoryModel(name = "Utilities", icon = R.drawable.ic_question_mark),
                CategoryModel(name = "Transportation", icon = R.drawable.ic_bus),
                CategoryModel(name = "Healthcare", icon = R.drawable.ic_health_shield),
                CategoryModel(name = "Education", icon = R.drawable.ic_book),
                CategoryModel(name = "Dining Out", icon = R.drawable.ic_spoon_fork),
                CategoryModel(name = "Entertainment", icon = R.drawable.ic_tv),
                CategoryModel(name = "Insurance", icon = R.drawable.ic_money_crossed),
                CategoryModel(name = "Clothing", icon = R.drawable.ic_question_mark),
                CategoryModel(name = "Travel", icon = R.drawable.ic_globe),
                CategoryModel(name = "Subscriptions", icon = R.drawable.ic_question_mark),
                CategoryModel(name = "Household Supplies", icon = R.drawable.ic_money_crossed),
                CategoryModel(name = "Pet Care", icon = R.drawable.ic_cat_paw),
                CategoryModel(name = "Miscellaneous", icon = R.drawable.ic_question_mark)
            )

            // Insert categories into the database
            categories.forEach { category ->
                categoryDao.insertCategory(category)
            }
        }

    }
}
