package com.example.wearosapp.repository

import androidx.lifecycle.LiveData
import com.example.wearosapp.databasedao.DogDao
import com.example.wearosapp.model.Dog
import kotlinx.coroutines.delay

class DogRepository (private var dogDao: DogDao?) {

    val dogs : LiveData<List<Dog>>? = dogDao?.dogs

    suspend fun insertNewDog (dog: Dog?) {
        val initialTime = System.currentTimeMillis().toInt()
        var currentTime = initialTime
        dog?.received = currentTime
        dog?.isOnline = true
        currentTime++
        delay(2)
        if (dog != null) {
            dogDao?.insertNewDog(dog)
        }

    }

    suspend fun insertNewDogs(dogs: List<Dog>) {
        val initialTime = System.currentTimeMillis().toInt()
        var currentTime = initialTime

        val existingDogs = this.dogs?.value
        existingDogs?.forEach {
            it.isOnline = false
            dogDao?.updateDog(it)
        }

        for (dog in dogs) {
            dog.received = currentTime
            dog.isOnline = true
            currentTime++
            delay(2)
            dogDao?.insertNewDog(dog)
        }
    }

    fun getDog(): LiveData<List<Dog>>? {
        return dogs
    }

    suspend fun updateDogs(dogs: List<Dog>) {
        dogDao?.updateDogs(dogs)
    }

    suspend fun updateDog(dog: Dog?) {
        dogDao?.updateDog(dog)
    }

    fun deleteDog(dogId : Long?) {
        dogDao?.deleteDog(dogId)
    }

    fun deleteAllDogsInDataBase() {
        dogDao?.deleteAllDogs()
    }
}