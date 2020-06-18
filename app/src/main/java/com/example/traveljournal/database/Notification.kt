package com.example.traveljournal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_table")
data class Notification(
    @PrimaryKey(autoGenerate = true)
    var notificationId: Long = 0L,

    @ColumnInfo(name = "notification_name")
    var notificationType: String = "",

    @ColumnInfo(name = "notification_state")
    var notificationState: Boolean = false
)