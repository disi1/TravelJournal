package com.example.traveljournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity(tableName = "memory_table")
data class Memory(
    @PrimaryKey(autoGenerate = true)
    var memoryId: Long = 0L,

    @ColumnInfo(name = "memory_name")
    var memoryName: String = "",

    @ColumnInfo(name = "memory_description")
    var memoryDescription: String = "",

    @ColumnInfo(name = "memory_timestamp")
    var memoryTimestamp: Long,

    @ColumnInfo(name = "cover_photo_src_uri")
    var coverPhotoSrcUri: String = "",

    @ColumnInfo(name = "experience_host_id")
    var experienceHostId: Long
)