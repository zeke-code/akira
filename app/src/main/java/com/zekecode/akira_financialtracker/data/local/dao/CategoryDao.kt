package com.zekecode.akira_financialtracker.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): LiveData<List<CategoryModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryModel)

    @Delete
    suspend fun deleteCategory(category: CategoryModel)
}
