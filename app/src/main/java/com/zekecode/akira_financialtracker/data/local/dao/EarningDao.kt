package com.zekecode.akira_financialtracker.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.EarningWithCategory

@Dao
interface EarningDao {
    @Query ("SELECT * FROM earnings")
    fun getAllEarnings(): LiveData<List<EarningModel>>

    @Transaction
    @Query("SELECT * FROM earnings")
    fun getAllEarningsWithCategories(): LiveData<List<EarningWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarning(earning: EarningModel)

    @Update
    suspend fun updateEarning(earning: EarningModel)

    @Delete
    suspend fun deleteEarning(earning: EarningModel)

    @Transaction
    @Query("SELECT * FROM earnings WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :yearMonth")
    fun getMonthlyEarningsWithCategories(yearMonth: String): LiveData<List<EarningWithCategory>>
}