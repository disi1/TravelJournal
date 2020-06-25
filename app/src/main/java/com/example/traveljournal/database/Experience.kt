package com.example.traveljournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "experience_table")
data class Experience(
    @PrimaryKey(autoGenerate = true)
    var experienceId: Long = 0L,

    @ColumnInfo(name = "experience_name")
    var experienceName: String = "",

    @ColumnInfo(name = "experience_place_name")
    var experiencePlaceName: String = "",

    @ColumnInfo(name = "experience_place_address")
    var experiencePlaceAddress: String = "",

    @ColumnInfo(name = "experience_description")
    var experienceDescription: String = "",

    @ColumnInfo(name = "cover_photo_src_uri")
    var coverPhotoSrcUri: String = "",

    @ColumnInfo(name = "cover_photo_attributions")
    var coverPhotoAttributions: String = "",

    @ColumnInfo(name = "journey_host_id")
    var journeyHostId: Long
)