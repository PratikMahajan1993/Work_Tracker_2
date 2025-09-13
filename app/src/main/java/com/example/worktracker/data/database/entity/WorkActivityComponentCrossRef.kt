package com.example.worktracker.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "work_activity_component_cross_ref",
    primaryKeys = ["workActivityId", "componentId"],
    foreignKeys = [
        ForeignKey(
            entity = WorkActivityLog::class, // Corrected to WorkActivityLog
            parentColumns = ["id"],
            childColumns = ["workActivityId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ComponentInfo::class,
            parentColumns = ["id"],
            childColumns = ["componentId"],
            onDelete = ForeignKey.CASCADE // Or RESTRICT if components should not be deleted if in use
        )
    ],
    indices = [Index(value = ["workActivityId"]), Index(value = ["componentId"])]
)
data class WorkActivityComponentCrossRef(
    val workActivityId: Long,
    val componentId: Long
)
