package com.example.worktracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "component_info",
    indices = [Index(value = ["component_name"], unique = true)]
)
data class ComponentInfo(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "component_name")
    val componentName: String,

    @ColumnInfo(name = "customer")
    val customer: String,

    @ColumnInfo(name = "priority_level")
    val priorityLevel: Int,

    @ColumnInfo(name = "cycle_time_minutes")
    val cycleTimeMinutes: Double, // Changed from Int to Double

    @ColumnInfo(name = "notes_for_ai")
    val notesForAi: String?
)
