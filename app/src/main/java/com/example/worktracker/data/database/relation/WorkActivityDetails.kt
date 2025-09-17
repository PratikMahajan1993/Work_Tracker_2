package com.example.worktracker.data.database.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.worktracker.data.database.entity.ActivityCategory
import com.example.worktracker.data.database.entity.ComponentInfo
// import com.example.worktracker.data.database.entity.TheBoysInfo // Removed import
import com.example.worktracker.data.database.entity.WorkActivityComponentCrossRef
import com.example.worktracker.data.database.entity.WorkActivityLog
// import com.example.worktracker.data.database.entity.WorkActivityTheBoyCrossRef // Removed import

data class WorkActivityDetails(
    @Embedded
    val workActivity: WorkActivityLog,

    @Relation(
        parentColumn = "categoryId", // Matches WorkActivityLog.categoryId
        entityColumn = "id"          // Matches ActivityCategory.id
    )
    val category: ActivityCategory?,

    // Removed TheBoysInfo relationship as it's incorrect for WorkActivityLog
    // @Relation(
    //     parentColumn = "id", 
    //     entity = TheBoysInfo::class,
    //     entityColumn = "boyId", 
    //     associateBy = Junction(
    //         value = WorkActivityTheBoyCrossRef::class,
    //         parentColumn = "workActivityId", 
    //         entityColumn = "theBoyId"      
    //     )
    // )
    // val theBoys: List<TheBoysInfo>,

    @Relation(
        parentColumn = "id", // Matches WorkActivityLog.id
        entity = ComponentInfo::class,
        entityColumn = "id", // Matches ComponentInfo.id - Assuming ComponentInfo.id is Long. If it's componentName, entityColumn should be "componentName"
        associateBy = Junction(
            value = WorkActivityComponentCrossRef::class,
            parentColumn = "workActivityId", // Matches WorkActivityComponentCrossRef.workActivityId
            entityColumn = "componentId"   // Matches WorkActivityComponentCrossRef.componentId
        )
    )
    val components: List<ComponentInfo>
)
