package com.example.traveljournal.database

import androidx.room.Embedded
import androidx.room.Relation

data class MemoryWithPhotos(
    @Embedded val memory: Memory,
    @Relation(
        parentColumn = "memoryId",
        entityColumn = "memory_host_id"
    )
    val memoryPhotos: List<MemoryPhoto>
)