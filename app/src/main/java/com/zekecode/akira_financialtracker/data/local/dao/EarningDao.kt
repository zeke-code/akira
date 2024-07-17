package com.zekecode.akira_financialtracker.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel

@Dao
interface EarningDao {
    @Query ("SELECT * FROM earnings")
    fun getAllEarnings(): LiveData<List<EarningModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarning(earning: EarningModel)

    @Delete
    suspend fun deleteEarning(earning: EarningModel)
}