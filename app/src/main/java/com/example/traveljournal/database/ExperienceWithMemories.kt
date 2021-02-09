package com.example.traveljournal.database

import androidx.room.Embedded
import androidx.room.Relation

data class ExperienceWithMemories(
    @Embedded val experience: Experience,
    @Relation(
        parentColumn = "experienceId",
        entityColumn = "experience_host_id"
    )
    val memories: List<Memory>
)