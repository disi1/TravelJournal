package com.example.traveljournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journey_table")
data class Journey(
    @PrimaryKey(autoGenerate = true)
    var journeyId: Long = 0L,
    @ColumnInfo(name = "place_id")
    var placeId: String = ""
)