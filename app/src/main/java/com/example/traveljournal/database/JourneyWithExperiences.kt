package com.example.traveljournal.database

import androidx.room.Embedded
import androidx.room.Relation

data class JourneyWithExperiences (
    @Embedded val journey: Journey,
    @Relation (
        parentColumn = "journeyId",
        entityColumn = "journey_host_id"
    )
    val experiences: List<Experience>
)