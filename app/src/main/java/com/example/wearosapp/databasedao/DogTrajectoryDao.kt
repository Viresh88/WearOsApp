package com.example.wearosapp.databasedao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wearosapp.model.DogTrajectory
import kotlinx.coroutines.flow.Flow

@Dao
interface DogTrajectoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrajectory(dogTrajectory: DogTrajectory?)

    @get:Query("select * from trajectory_table order by dateTime ASC")
    val allDogsTrajectory : Flow<List<DogTrajectory>>

    @Query("DELETE FROM trajectory_table")
    fun deleteAllTrajectory()
}