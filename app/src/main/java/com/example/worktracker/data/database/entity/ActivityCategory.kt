package com.example.worktracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_categories")
data class ActivityCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis()
)
