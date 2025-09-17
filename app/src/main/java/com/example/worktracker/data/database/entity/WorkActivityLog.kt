package com.example.worktracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_activity_logs",
    foreignKeys = [
        ForeignKey(
            entity = ActivityCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL // Or another action like RESTRICT or CASCADE as per your needs
        )
    ],
    indices = [Index(value = ["categoryId"])] // Index for the new foreign key
)
data class WorkActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryName: String, // Kept for potential direct use, but relationship is via categoryId
    var categoryId: Long? = null, // New foreign key to ActivityCategory
    val startTime: Long,
    val endTime: Long?,
    val description: String,
    val operatorId: Int? = null, // Making it nullable if it can be empty
    val expenses: Double? = null, // Making it nullable if it can be empty
    val logDate: Long,
    val taskSuccessful: Boolean? = null,
    val assignedBy: String? = null,
    val duration: Long? = null,
    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis()
) {
    @Ignore // For syncing relationships from Firestore
    var componentIdsForSync: List<Long>? = null
}
