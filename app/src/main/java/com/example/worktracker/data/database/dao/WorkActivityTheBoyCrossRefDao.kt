package com.example.worktracker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.worktracker.data.database.entity.WorkActivityTheBoyCrossRef

@Dao
interface WorkActivityTheBoyCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(crossRef: WorkActivityTheBoyCrossRef): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(crossRefs: List<WorkActivityTheBoyCrossRef>): List<Long>

    @Query("DELETE FROM work_activity_the_boy_cross_ref WHERE workActivityId = :workActivityId")
    suspend fun deleteByWorkActivityId(workActivityId: Long)

    @Query("SELECT theBoyId FROM work_activity_the_boy_cross_ref WHERE workActivityId = :workActivityId")
    suspend fun getTheBoyIdsForWorkActivity(workActivityId: Long): List<Long>
}
