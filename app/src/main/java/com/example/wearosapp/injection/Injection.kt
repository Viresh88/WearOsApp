package com.example.wearosapp.injection

import android.content.Context
import com.example.wearosapp.database.SaveDatabase
import com.example.wearosapp.repository.DeviceRepository
import com.example.wearosapp.repository.DogRepository
import com.example.wearosapp.repository.FenceRepository
import com.example.wearosapp.repository.MarkerRepository
import com.example.wearosapp.repository.PositionRepository
import com.example.wearosapp.repository.TrajectoryRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

open class Injection {
    companion object {
        private fun provideDogData(context: Context?): DogRepository {
            val database: SaveDatabase? = context?.let {
                SaveDatabase.getInstance(it)
            }

            return DogRepository(database?.dogDao())
        }

        private fun provideDeviceData(context: Context?): DeviceRepository {
            val database: SaveDatabase? = context?.let {
                SaveDatabase.getInstance(it)
            }
            return DeviceRepository(database?.deviceDao())
        }

        private fun provideTrajectoryData(context: Context?): TrajectoryRepository {
            val database: SaveDatabase? = context?.let {
                SaveDatabase.getInstance(it)
            }
            return TrajectoryRepository(database?.dogTrajectoryDao())
        }

        private fun providePositionData(context: Context?): PositionRepository {
            val database: SaveDatabase? = context?.let {
                SaveDatabase.getInstance(it)
            }
            return PositionRepository(database?.positionDao())
        }

        private fun provideFenceData(context: Context?): FenceRepository {
            val database: SaveDatabase? = context?.let {
                SaveDatabase.getInstance(it)
            }
            return FenceRepository(database?.fenceDao())
        }

        private fun provideMarkerData(context: Context?): MarkerRepository {
            val database: SaveDatabase? = context?.let {
                SaveDatabase.getInstance(it)
            }
            return MarkerRepository(database?.markerDao())
        }

        private fun provideExecutor(): Executor {
            return Executors.newSingleThreadExecutor()
        }

        fun provideViewModelFactory(context: Context?): ViewModelFactory {
            val dogRepository : DogRepository = provideDogData(context)
            val bluetoothDeviceRepository: DeviceRepository = provideDeviceData(context)
            val trajectoryRepository : TrajectoryRepository = provideTrajectoryData(context)
            val positionRepository : PositionRepository = providePositionData(context)
            val fenceRepository : FenceRepository = provideFenceData(context)
            val markerRepository : MarkerRepository = provideMarkerData(context)

            val executor = provideExecutor()
            return ViewModelFactory(dogRepository,bluetoothDeviceRepository, trajectoryRepository, positionRepository, fenceRepository, markerRepository, executor)
        }
    }
}