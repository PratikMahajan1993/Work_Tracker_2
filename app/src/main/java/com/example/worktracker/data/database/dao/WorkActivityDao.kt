package com.example.worktracker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.database.relation.WorkActivityDetails // Import for WorkActivityDetails
import com.example.worktracker.data.database.entity.WorkActivityComponentCrossRef // Corrected import
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkActivityLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllWorkActivityLogs(logs: List<WorkActivityLog>) // New method for SyncWorker

    // Method to get all WorkActivityLog entities, needed by UploadAllDataWorker
    @Query("SELECT * FROM work_activity_logs ORDER BY startTime DESC")
    fun getAllWorkActivityLogs(): Flow<List<WorkActivityLog>>

    @Transaction
    @Query("SELECT * FROM work_activity_logs ORDER BY startTime DESC")
    fun getAllWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>>

    @Transaction
    @Query("SELECT * FROM work_activity_logs WHERE id = :id")
    suspend fun getWorkActivityWithDetailsById(id: Long): WorkActivityDetails?

    // Method to get component IDs for a specific WorkActivityLog, needed by UploadAllDataWorker
    // Assumes your cross-reference table is named 'work_activity_component_cross_ref'
    // and has columns 'workActivityId' and 'componentId'
    @Query("SELECT componentId FROM work_activity_component_cross_ref WHERE workActivityId = :workActivityId")
    fun getComponentIdsForWorkActivity(workActivityId: Long): Flow<List<Long>>

    @Query("DELETE FROM work_activity_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM work_activity_logs")
    suspend fun clearAllLogsInternal()

    @Query("DELETE FROM sqlite_sequence WHERE name='work_activity_logs'")
    suspend fun resetAutoIncrementForLogsTable()

    @Transaction
    suspend fun wipeDatabaseAndResetIds() {
        clearAllLogsInternal()
        resetAutoIncrementForLogsTable()
    }

    @Transaction
    @Query("SELECT * FROM work_activity_logs WHERE categoryName = :categoryName AND endTime IS NULL LIMIT 1")
    fun getOngoingActivityWithDetailsByCategoryName(categoryName: String): Flow<WorkActivityDetails?> // New method

    @Query("SELECT * FROM work_activity_logs WHERE endTime IS NULL")
    fun getOngoingActivities(): Flow<List<WorkActivityLog>>

    @Transaction
    @Query("SELECT * FROM work_activity_logs ORDER BY startTime DESC LIMIT 5")
    fun getRecentWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>>

    @Query("UPDATE work_activity_logs SET categoryName = :newCategoryName WHERE categoryName = :oldCategoryName")
    suspend fun updateCategoryNameForLogs(oldCategoryName: String, newCategoryName: String)
}
