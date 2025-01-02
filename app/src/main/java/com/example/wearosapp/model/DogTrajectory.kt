package com.example.wearosapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "trajectory_table")
data class DogTrajectory(
    val imei: String ,
    val latitude: Double ,
    val longitude: Double ,
    val altitude: Double ,
    val batteryPower: Int ,
    val status: Int ,
    val dateTime: Long ,

    @PrimaryKey(autoGenerate = true)
    var id: Long? = System.currentTimeMillis()
)
