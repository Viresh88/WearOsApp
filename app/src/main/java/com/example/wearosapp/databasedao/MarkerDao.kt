package com.example.wearosapp.databasedao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wearosapp.model.MarkerPosition
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMarker(markerPosition: MarkerPosition?)

    @Query("SELECT * FROM marker_table")
    fun getMarker(): Flow<List<MarkerPosition>>

    @Update
    suspend fun updateMarker(markerPosition: MarkerPosition?)

    @Query("DELETE FROM marker_table WHERE markerId = :markerId ")
    fun deleteMarker(markerId:Long?)

    @Query("DELETE FROM marker_table")
    fun deleteAllMarker()
}