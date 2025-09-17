package com.example.worktracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "the_boys_info")
data class TheBoysInfo(
    @PrimaryKey val boyId: Int,
    val name: String,
    val role: String,
    val notes: String?,
    val notesForAi: String?,
    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis()
)
