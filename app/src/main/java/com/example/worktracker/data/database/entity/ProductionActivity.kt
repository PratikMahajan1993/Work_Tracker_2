package com.example.worktracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "production_activity",
    foreignKeys = [
        ForeignKey(
            entity = TheBoysInfo::class,
            parentColumns = ["boyId"], // Assuming "boyId" is the primary key column in TheBoysInfo
            childColumns = ["boyId"],
            onDelete = ForeignKey.SET_NULL // Or RESTRICT/CASCADE as per your long-term needs
        )
    ],
    indices = [Index(value = ["boyId"])]
)
data class ProductionActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boyId: Int?, // Making it nullable to align with onDelete = ForeignKey.SET_NULL
    val componentName: String,
    val machineNumber: Int,
    val productionQuantity: Int,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val downtimeMinutes: Int? = null,
    val rejectionQuantity: Int? = null,
    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis()
)
