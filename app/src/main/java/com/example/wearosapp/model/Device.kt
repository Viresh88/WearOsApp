package com.example.wearosapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "device_table")
data class Device (
    var name: String? = null ,
    var address: String? = null ,
    var status : Boolean = false ,
    var bluetoothStatus: String? = null ,
    var lastConnected: String? = null,

    @PrimaryKey(autoGenerate = true)
    var id: Long? = address.hashCode().toLong()
)