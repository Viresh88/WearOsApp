package com.example.wearosapp.databasedao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.wearosapp.model.Position
import kotlinx.coroutines.flow.Flow

@Dao
interface PositionDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPosition(position: Position?)

    @Query("SELECT * FROM position_table ORDER BY positionId")
    fun getPosition(): Flow<List<Position>>

    @Update
    suspend fun updatePositions(positions : List<Position>)

    @Query("DELETE FROM position_table WHERE positionId = :positionId")
    fun deletePosition(positionId:Long?)

    @Query("DELETE FROM position_table WHERE fk_fenceId = :fk_fenceId")
    fun deletePositionFence (fk_fenceId : Long?)

    @Query("DELETE FROM position_table")
    fun deleteAllPositions()
}