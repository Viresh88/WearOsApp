package com.example.wearosapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.wearosapp.bluetooth.FormatCommand
import com.example.wearosapp.model.Device
import com.example.wearosapp.model.Dog
import com.example.wearosapp.model.DogTrajectory
import com.example.wearosapp.model.Fence
import com.example.wearosapp.model.FenceToPosition
import com.example.wearosapp.model.MarkerPosition
import com.example.wearosapp.model.Position
import com.example.wearosapp.repository.DeviceRepository
import com.example.wearosapp.repository.DogRepository
import com.example.wearosapp.repository.FenceRepository
import com.example.wearosapp.repository.MarkerRepository
import com.example.wearosapp.repository.PositionRepository
import com.example.wearosapp.repository.TrajectoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

class DogViewModel(
    private val dogRepository: DogRepository? ,
    private val deviceRepository: DeviceRepository? ,
    private val trajectoryRepository: TrajectoryRepository? ,
    private val positionRepository: PositionRepository? ,
    private val fenceRepository: FenceRepository? ,
    private val markerRepository: MarkerRepository? ,
    private val executor: Executor?
) : ViewModel() {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var listDog = mutableListOf<Dog>()
    private var updatedDogList = mutableListOf<Dog>()
    private val allDevices = deviceRepository?.getDevices()?.asLiveData()
    private val allDogs = dogRepository?.getDog()
    private val _selectedDog = MutableLiveData<Dog?>()

    fun updateAllDogs(updatedDogs: List<Dog>) {
        coroutineScope.launch {
            dogRepository?.updateDogs(updatedDogs)
            updateDogList(updatedDogs)
        }
    }

    fun insertNewDog(dog: Dog?) {
        coroutineScope.launch {
            dogRepository?.insertNewDog(dog)
            updateDogList(listOfNotNull(dog))
        }
    }

    fun insertNewDogs(dogs: List<Dog>?) {
        coroutineScope.launch   {
            dogs?.let { dogRepository?.insertNewDogs(it) }
            updateDogList(dogs ?: emptyList())
        }
    }

    fun getAllDogs(): LiveData<List<Dog>>? {
        return allDogs
    }

    fun updateDog(dog: Dog) {
        coroutineScope.launch {
            dogRepository?.updateDog(dog)
            val updatedList = listDog.map { if (it.id == dog.id) dog else it }
            updateDogList(updatedList)
        }
    }

    fun deleteDog(dogId: Long) {
        dogRepository?.deleteDog(dogId)
        val updatedList = listDog.filterNot { it.id == dogId }
        updateDogList(updatedList)
    }

    private fun updateDogList(updatedList: List<Dog>) {
        for (updatedDog in updatedList) {
            val existingDog = listDog.singleOrNull { it.imei == updatedDog.imei }
            if (existingDog != null) {
                val latitudeChanged = existingDog.latitude != updatedDog.latitude && updatedDog.latitude != 0.0
                val longitudeChanged = existingDog.longitude != updatedDog.longitude && updatedDog.longitude != 0.0
                if (latitudeChanged || longitudeChanged) {
                    existingDog.latitude = existingDog.latitude
                    existingDog.longitude = existingDog.longitude
                    existingDog.latitude = updatedDog.latitude
                    existingDog.longitude = updatedDog.longitude
                }
            } else {
                updatedDogList.add(updatedDog)
            }
        }
    }


    fun deleteAllDogs() {
        executor?.execute {
            dogRepository?.deleteAllDogsInDataBase()
        }
    }

    fun renameBleDevice(newName : String){
        viewModelScope.launch {
            deviceRepository?.renameLastConnectedDevices(newName)
        }
    }

    fun createNewPosition(position: Position?) {
        coroutineScope.launch {
            positionRepository?.createPosition(position)
        }
    }

    fun getAllPositions(): LiveData<List<Position>>? {
        return positionRepository?.getAllPositions()?.asLiveData()
    }

    fun deletePosition(positionId: Long) {
        executor?.execute {
            positionRepository?.deletePosition(positionId)
        }
    }

    fun deletePositionFence(fk_fenceId: Long) {
        executor?.execute {
            positionRepository?.deletePositionFence(fk_fenceId)
        }
    }

    fun deleteAllPositions() {
        executor?.execute {
            positionRepository?.deleteAllPositions()
        }
    }

    fun updatePositions(list: List<Position>) {
        coroutineScope.launch {
            positionRepository?.updatePositions(list)
        }
    }

    fun createNewDevice(device: Device?) {
        coroutineScope.launch {
            deviceRepository?.createDevice(device)
        }
    }

    fun getAllDevices(): LiveData<List<Device>>? {
        return allDevices
    }

    suspend fun getLastConnectedDevice(): List<Device>? {
        return withContext(Dispatchers.IO) {
            deviceRepository?.getDevicesWithLastConnectedTrue()
        }
    }

    fun getStatusByMacAddress(macAddress: String, onStatusReceived: (Boolean?) -> Unit) {
        viewModelScope.launch {
            val status = deviceRepository?.getStatusByMacAddress(macAddress)
            onStatusReceived(status)
        }
    }

    fun updateDevice(device: Device) {
        coroutineScope.launch {
            deviceRepository?.updateDevice(device)
        }
    }

    fun deleteDevice(deviceId: Long) {
        executor?.execute {
            deviceRepository?.deletedDevice(deviceId)
        }
    }

    fun  deleteAllDevices () {
        executor?.execute {
            deviceRepository?.deleteAllDevice()
        }
    }

    fun createNewTrajectory(dogTrajectory: DogTrajectory?) {
        coroutineScope.launch{
            trajectoryRepository?.createTrajectory(dogTrajectory)
        }
    }


    fun updateDogTrajectory(dogTrajectory: DogTrajectory, dogs: ArrayList<Dog>) {
        coroutineScope.launch {
            val dogToUpdateIndex = dogs.indexOfFirst { it.imei == dogTrajectory.imei }
            if (dogToUpdateIndex != -1) {
                val dogToUpdate = dogs[dogToUpdateIndex]
                dogToUpdate.latitude =  dogToUpdate.latitude
                dogToUpdate.longitude =  dogToUpdate.longitude
                dogToUpdate.latitude = dogTrajectory.latitude
                dogToUpdate.longitude = dogTrajectory.longitude
                dogToUpdate.power = dogTrajectory.batteryPower
                dogToUpdate.status = dogTrajectory.status
                dogToUpdate.time = dogTrajectory.dateTime

                updateDog(dogToUpdate)
                // Notify UI of updated dog data
                FormatCommand.onFormatData?.onDogData(dogs)
            }

            // Notify UI of GPS data update
            FormatCommand.onFormatData?.onGpsData(dogTrajectory)

            // Assuming these methods perform asynchronous operations, ensure they're awaited properly
            createNewTrajectory(dogTrajectory)
            updateAllDogs(dogs)
        }
    }


    fun getAllTrajectory(): LiveData<List<DogTrajectory>>? {
        return trajectoryRepository?.getTrajectory()?.asLiveData()
    }

    fun deleteAllTrajectory() {
        executor?.execute {
            trajectoryRepository?.deleteAllTrajectory()
        }
    }

    fun createNewFence(fence: Fence?) {
        coroutineScope.launch {
            fenceRepository?.createFence(fence)
        }
    }

    fun getFence(): LiveData<List<Fence>>? {
        return fenceRepository?.getFence()?.asLiveData()
    }

    fun getAllFenceToPositions(): LiveData<List<FenceToPosition>>? {
        return fenceRepository?.getFenceToPosition()?.asLiveData()
    }

    fun updateFence(fence: Fence?) {
        coroutineScope.launch {
            fenceRepository?.updateFence(fence)
        }
    }

    fun deleteFence(fenceId: Long?) {
        executor?.execute {
            fenceRepository?.deleteFence(fenceId!!)
        }
    }

    fun deleteAllFence() {
        executor?.execute {
            fenceRepository?.deleteAllFence()
        }
    }

    fun createNewMarker(markerPosition: MarkerPosition?) {
        coroutineScope.launch {
            markerRepository?.createMarker(markerPosition)
        }
    }

    fun getAllMarkers(): LiveData<List<MarkerPosition>>? {
        return markerRepository?.getMarker()?.asLiveData()
    }

    fun updateMarker(markerPosition: MarkerPosition?) {
        coroutineScope.launch {
            markerRepository?.updateMarker(markerPosition)
        }
    }

    fun deleteMarker(markerId: Long?) {
        executor?.execute {
            markerRepository?.deleteMarker(markerId)
        }
    }

    fun deleteAllMarkers() {
        executor?.execute {
            markerRepository?.deleteAllMarker()
        }
    }

    fun updateLastConnectedDevice(address: String) {
        coroutineScope.launch {
            deviceRepository?.updateLastConnectedDevices(address)
        }
    }
}