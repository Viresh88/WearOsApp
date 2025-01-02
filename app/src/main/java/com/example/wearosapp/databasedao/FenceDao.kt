package com.example.wearosapp.databasedao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import com.example.wearosapp.model.Fence
import com.example.wearosapp.model.FenceToPosition
import kotlinx.coroutines.flow.Flow

@Dao
interface FenceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFence(fence: Fence?)

    @Query("SELECT * FROM fence_table ORDER BY fenceId")
    fun getFence(): Flow<List<Fence>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query("SELECT * FROM fence_table  JOIN position_table ON fk_fenceId = fenceId")
    fun getFenceToPosition () : Flow<List<FenceToPosition>>

    @Update
    suspend fun updateFence(fence: Fence?)

    @Query("DELETE FROM fence_table WHERE fenceId = :fenceId ")
    fun deleteFence(fenceId:Long?)

    @Query("DELETE FROM fence_table")
    fun deleteAllFence()

}