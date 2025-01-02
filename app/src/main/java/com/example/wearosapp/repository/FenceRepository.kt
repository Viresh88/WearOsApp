package com.example.wearosapp.repository

import com.example.wearosapp.databasedao.FenceDao
import com.example.wearosapp.model.Fence
import com.example.wearosapp.model.FenceToPosition
import kotlinx.coroutines.flow.Flow

class FenceRepository (private var fenceDao: FenceDao?) {

    suspend fun createFence(fence: Fence?) {
        fenceDao?.insertFence(fence)
    }

    fun getFence(): Flow<List<Fence>>? {
        return fenceDao?.getFence()
    }

    fun getFenceToPosition() : Flow<List<FenceToPosition>>? {
        return fenceDao?.getFenceToPosition()
    }

    suspend fun updateFence(fence: Fence?) {
        fenceDao?.updateFence(fence)
    }

    fun deleteFence(fenceIdeId: Long?) {
        fenceDao?.deleteFence(fenceIdeId)
    }

    fun deleteAllFence() {
        fenceDao?.deleteAllFence()
    }
}