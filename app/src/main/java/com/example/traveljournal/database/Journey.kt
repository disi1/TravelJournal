package com.example.traveljournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journey_table")
data class Journey(
    @PrimaryKey(autoGenerate = true)
    var journeyId: Long = 0L,

    @ColumnInfo(name = "place_name")
    var placeName: String = "",

    @ColumnInfo(name = "place_address")
    var placeAddress: String = "",

    @ColumnInfo(name = "cover_photo_src_uri")
    var coverPhotoSrcUri: String = "",

    @ColumnInfo(name = "cover_photo_attributions")
    var coverPhotoAttributions: String = ""
)