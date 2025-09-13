package com.example.worktracker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.database.relation.WorkActivityDetails // Import for WorkActivityDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkActivityLog): Long

    @Transaction
    @Query("SELECT * FROM work_activity_logs ORDER BY startTime DESC")
    fun getAllWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>>

    @Transaction
    @Query("SELECT * FROM work_activity_logs WHERE id = :id")
    suspend fun getWorkActivityWithDetailsById(id: Long): WorkActivityDetails?

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

    // Replaced by getOngoingActivityWithDetailsByCategoryName
    // @Query("SELECT * FROM work_activity_logs WHERE categoryName = :categoryName AND endTime IS NULL LIMIT 1")
    // fun getOngoingActivityByCategoryName(categoryName: String): Flow<WorkActivityLog?>

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
