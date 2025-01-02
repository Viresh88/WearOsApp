package com.example.wearosapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "marker_table")
data class MarkerPosition(

    var latitude: Double,
    var longitude: Double,
    var icon: Int,
    var tag: String,
    var color : Int = 0,

    @PrimaryKey(autoGenerate = true)
    var markerId: Long = System.currentTimeMillis()

)
