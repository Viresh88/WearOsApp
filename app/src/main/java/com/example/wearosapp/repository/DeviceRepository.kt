package com.example.wearosapp.repository

import com.example.wearosapp.databasedao.DeviceDao
import com.example.wearosapp.model.Device
import kotlinx.coroutines.flow.Flow

class DeviceRepository(private var deviceDao: DeviceDao?) {
    private val allDevices : Flow<List<Device>>? = deviceDao?.allDevices

    suspend fun createDevice(device: Device?) {
        deviceDao?.insertDevice(device)
    }

    fun getDevices(): Flow<List<Device>>? {
        return allDevices
    }

    suspend fun updateDevice(device: Device) {
        deviceDao?.updateDevice(device)
    }

    fun deletedDevice(deviceId: Long?) {
        deviceDao?.deleteDevice(deviceId)
    }

    fun deleteAllDevice() {
        deviceDao?.deleteAllDevice()
    }

    suspend fun getDevicesWithLastConnectedTrue(): List<Device>? {
        return deviceDao?.getDevicesWithLastConnectedTrue()
    }

    suspend fun updateLastConnectedDevices(connectedAddress: String){
        deviceDao?.updateLastConnectedDevice(connectedAddress)
    }

    suspend fun renameLastConnectedDevices(newDeviceName: String){
        deviceDao?.renameLastConnectedDevices(newDeviceName)
    }


    suspend fun getStatusByMacAddress(macAddress: String): Boolean? {
        return deviceDao?.getStatusByMacAddress(macAddress)
    }
}