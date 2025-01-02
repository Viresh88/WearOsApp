package com.example.wearosapp.repository

import com.example.wearosapp.databasedao.PositionDao
import com.example.wearosapp.model.Position
import kotlinx.coroutines.flow.Flow

class PositionRepository (private var positionDao: PositionDao?) {

    suspend fun createPosition (position: Position?) {
        positionDao?.insertPosition(position)
    }

    fun getAllPositions() : Flow<List<Position>>? {
        return positionDao?.getPosition()
    }

    fun deletePosition(positionId : Long?) {
        positionDao?.deletePosition(positionId)
    }

    fun deletePositionFence(fk_fenceId : Long?) {
        positionDao?.deletePositionFence (fk_fenceId)
    }

    fun deleteAllPositions() {
        positionDao?.deleteAllPositions()
    }

    suspend fun updatePositions(list: List<Position>) {
        positionDao?.updatePositions(list)
    }
}