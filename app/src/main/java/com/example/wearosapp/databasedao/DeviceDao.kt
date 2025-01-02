package com.example.wearosapp.databasedao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wearosapp.model.Device
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDevice(device: Device?)

    @get:Query("SELECT * FROM device_table ORDER BY name ASC")
    val allDevices : Flow<List<Device>>

    @Update
    suspend fun updateDevice(devices : Device)

    @Query("DELETE FROM device_table WHERE id = :deviceId ")
    fun deleteDevice(deviceId:Long?)

    @Query("DELETE FROM device_table")
    fun deleteAllDevice()


    @Query("SELECT * FROM device_table WHERE lastConnected = 'Connected'")
    fun getDevicesWithLastConnectedTrue(): List<Device>


    @Query("UPDATE device_table SET lastConnected = CASE WHEN address = :connectedAddress THEN 'Connected' ELSE 'disconnected' END")
    suspend fun updateLastConnectedDevice(connectedAddress: String)


    @Query("UPDATE device_table SET name = :newName WHERE lastConnected = 'Connected'")
    suspend fun renameLastConnectedDevices(newName: String)



    @Query("SELECT status FROM device_table WHERE address = :macAddress")
    suspend fun getStatusByMacAddress(macAddress: String): Boolean?
}