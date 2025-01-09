package com.example.wearosapp.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wearosapp.repository.DeviceRepository
import com.example.wearosapp.repository.DogRepository
import com.example.wearosapp.repository.FenceRepository
import com.example.wearosapp.repository.MarkerRepository
import com.example.wearosapp.repository.PositionRepository
import com.example.wearosapp.repository.TrajectoryRepository
import com.example.wearosapp.viewmodel.DogViewModel
import java.util.concurrent.Executor

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private var dogRepository: DogRepository,
                       private var deviceRepository: DeviceRepository,
                       private var trajectoryRepository: TrajectoryRepository,
                       private var positionRepository: PositionRepository,
                       private var fenceRepository: FenceRepository,
                       private var markerRepository: MarkerRepository,
                       private var executor: Executor?,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DogViewModel::class.java)) {
            return DogViewModel(
                dogRepository,
                deviceRepository,
                trajectoryRepository,
                positionRepository,
                fenceRepository,
                markerRepository,
                executor
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}