package com.example.wearosapp.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
data class FenceToPosition(
    @Embedded val fence: Fence,
    @Relation(
        parentColumn = "fenceId",
        entityColumn = "fk_fenceId",
        entity = Position::class
    )
    val positions: List<Position>
)
