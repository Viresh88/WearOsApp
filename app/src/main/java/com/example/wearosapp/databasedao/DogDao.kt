package com.example.wearosapp.databasedao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wearosapp.model.Dog

@Dao
interface DogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNewDog(dog: Dog)

    @get:Query("select * from dog_table order by received ASC")
    val dogs : LiveData<List<Dog>>

    @Update
    fun updateDogs(dogs: List<Dog>)

    @Update
    fun updateDog(dog: Dog?)

    @Query("DELETE FROM dog_table WHERE id = :dogId ")
    fun deleteDog(dogId:Long?)

    @Query("DELETE FROM dog_table")
    fun deleteAllDogs()


}