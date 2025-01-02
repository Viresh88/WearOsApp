package com.example.wearosapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Dog_table")
data class Dog(
    var imei: String? ,
    var name: String? ,
    var color: Int = 0 ,
    var ledLight: Int = 0 ,
    var collarVersion: Int = 0 ,
    var power: Int = 0 ,
    var status: Int = 0 ,
    var angle: Float = 0F ,
    var latitude: Double = 0.0 ,
    var longitude: Double = 0.0 ,
    var time: Long = System.currentTimeMillis() ,
    var isSelected: Boolean = false ,
    var isOnline: Boolean = false ,
    var fenceId: Long = 0 ,
    var levelSanction: Int = 0,
    var received: Int = 0,

    @PrimaryKey(autoGenerate = true)
    var id: Long = imei.hashCode().toLong()
)
