package com.example.worktracker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.worktracker.data.database.entity.WorkActivityComponentCrossRef

@Dao
interface WorkActivityComponentCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(crossRef: WorkActivityComponentCrossRef): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(crossRefs: List<WorkActivityComponentCrossRef>): List<Long>

    @Query("DELETE FROM work_activity_component_cross_ref WHERE workActivityId = :workActivityId")
    suspend fun deleteByWorkActivityId(workActivityId: Long)

    @Query("DELETE FROM work_activity_component_cross_ref WHERE workActivityId = :workActivityId AND componentId = :componentId")
    suspend fun delete(workActivityId: Long, componentId: Long)

    @Query("SELECT componentId FROM work_activity_component_cross_ref WHERE workActivityId = :workActivityId")
    suspend fun getComponentIdsForWorkActivity(workActivityId: Long): List<Long>
}
