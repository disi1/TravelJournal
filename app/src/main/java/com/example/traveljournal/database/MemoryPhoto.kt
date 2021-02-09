package com.example.traveljournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_photo_table")
data class MemoryPhoto(
    @PrimaryKey(autoGenerate = true)
    var photoId: Long = 0L,

    @ColumnInfo(name = "photo_src_uri")
    var photoSrcUri: String = "",

    @ColumnInfo(name = "photo_caption")
    var photoCaption: String = "",

    @ColumnInfo(name = "memory_host_id")
    var memoryHostId: Long
)