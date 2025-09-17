package com.example.worktracker.data.repository

import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.database.relation.WorkActivityDetails
import kotlinx.coroutines.flow.Flow

interface WorkActivityRepository {
    suspend fun insertWorkActivity(log: WorkActivityLog, componentIds: List<Long>): Long // Removed theBoyIds
    fun getAllWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>>
    suspend fun getWorkActivityWithDetailsById(id: Long): WorkActivityDetails?
    suspend fun deleteLogById(id: Long)
    suspend fun clearAllLogs()
    fun getOngoingActivityByCategoryName(categoryName: String): Flow<WorkActivityDetails?>
    fun getOngoingActivities(): Flow<List<WorkActivityLog>>
    fun getRecentWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>>
    suspend fun updateCategoryNameForExistingLogs(oldCategoryName: String, newCategoryName: String)
}
