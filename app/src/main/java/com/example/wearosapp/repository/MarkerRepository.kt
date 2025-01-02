package com.example.wearosapp.repository

import com.example.wearosapp.databasedao.MarkerDao
import com.example.wearosapp.model.MarkerPosition
import kotlinx.coroutines.flow.Flow

class MarkerRepository (private var markerDao: MarkerDao?) {

    suspend fun createMarker(markerPosition: MarkerPosition?) {
        markerDao?.insertMarker(markerPosition)
    }

    fun getMarker(): Flow<List<MarkerPosition>>? {
        return markerDao?.getMarker()
    }

    suspend fun updateMarker(markerPosition: MarkerPosition?) {
        markerDao?.updateMarker(markerPosition)
    }

    fun deleteMarker(markerId: Long?) {
        markerDao?.deleteMarker(markerId)
    }

    fun deleteAllMarker() {
        markerDao?.deleteAllMarker()
    }
}