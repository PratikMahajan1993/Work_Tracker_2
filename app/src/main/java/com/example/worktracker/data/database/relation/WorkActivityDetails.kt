package com.example.worktracker.data.database.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.worktracker.data.database.entity.ActivityCategory
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.database.entity.TheBoysInfo
import com.example.worktracker.data.database.entity.WorkActivityComponentCrossRef
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.database.entity.WorkActivityTheBoyCrossRef

data class WorkActivityDetails(
    @Embedded
    val workActivity: WorkActivityLog,

    @Relation(
        parentColumn = "categoryId", // Matches WorkActivityLog.categoryId
        entityColumn = "id"          // Matches ActivityCategory.id
    )
    val category: ActivityCategory?,

    @Relation(
        parentColumn = "id", // Matches WorkActivityLog.id
        entity = TheBoysInfo::class,
        entityColumn = "boyId", // Matches TheBoysInfo.boyId
        associateBy = Junction(
            value = WorkActivityTheBoyCrossRef::class,
            parentColumn = "workActivityId", // Matches WorkActivityTheBoyCrossRef.workActivityId
            entityColumn = "theBoyId"      // Matches WorkActivityTheBoyCrossRef.theBoyId
        )
    )
    val theBoys: List<TheBoysInfo>,

    @Relation(
        parentColumn = "id", // Matches WorkActivityLog.id
        entity = ComponentInfo::class,
        entityColumn = "id", // Matches ComponentInfo.id
        associateBy = Junction(
            value = WorkActivityComponentCrossRef::class,
            parentColumn = "workActivityId", // Matches WorkActivityComponentCrossRef.workActivityId
            entityColumn = "componentId"   // Matches WorkActivityComponentCrossRef.componentId
        )
    )
    val components: List<ComponentInfo>
)
