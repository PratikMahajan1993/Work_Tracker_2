package com.example.worktracker.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "work_activity_the_boy_cross_ref",
    primaryKeys = ["workActivityId", "theBoyId"],
    foreignKeys = [
        ForeignKey(
            entity = WorkActivityLog::class, // Ensure this matches your WorkActivityLog entity name
            parentColumns = ["id"],
            childColumns = ["workActivityId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TheBoysInfo::class,
            parentColumns = ["boyId"], // Ensure this matches the primary key in TheBoysInfo
            childColumns = ["theBoyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workActivityId"]), Index(value = ["theBoyId"])]
)
data class WorkActivityTheBoyCrossRef(
    val workActivityId: Long,
    val theBoyId: Long // Assuming theBoyId in TheBoysInfo is Long
)
