package com.example.worktracker.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "production_activity")
data class ProductionActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boyId: Int,
    val componentName: String,
    val machineNumber: Int,
    val productionQuantity: Int,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val downtimeMinutes: Int? = null, // Added this line
    val rejectionQuantity: Int? = null // New field for rejection quantity
)
