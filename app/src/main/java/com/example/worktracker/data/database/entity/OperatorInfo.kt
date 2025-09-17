package com.example.worktracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operator_info")
data class OperatorInfo(
    @PrimaryKey(autoGenerate = true)
    val operatorId: Int = 0, // Now auto-generated
    val name: String,
    val hourlySalary: Double,
    val role: String,
    val priority: Int, // e.g., 1-5 scale
    val notes: String?,
    val notesForAi: String?,
    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis()
)
