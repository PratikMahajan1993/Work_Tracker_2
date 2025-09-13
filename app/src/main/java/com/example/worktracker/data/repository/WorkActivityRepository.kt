package com.example.worktracker.data.repository

import com.example.worktracker.data.database.dao.WorkActivityDao
import com.example.worktracker.data.database.dao.WorkActivityComponentCrossRefDao
import com.example.worktracker.data.database.dao.WorkActivityTheBoyCrossRefDao // New DAO import
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.database.entity.WorkActivityComponentCrossRef
import com.example.worktracker.data.database.entity.WorkActivityTheBoyCrossRef // New Entity import
import com.example.worktracker.data.database.relation.WorkActivityDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface WorkActivityRepository {
    suspend fun insertWorkActivity(log: WorkActivityLog, componentIds: List<Long>, theBoyIds: List<Long>): Long // Added theBoyIds
    fun getAllWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>>
    suspend fun getWorkActivityWithDetailsById(id: Long): WorkActivityDetails?
    suspend fun deleteLogById(id: Long)
    suspend fun clearAllLogs()
    fun getOngoingActivityByCategoryName(categoryName: String): Flow<WorkActivityDetails?>
    fun getOngoingActivities(): Flow<List<WorkActivityLog>>
    fun getRecentWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>>
    suspend fun updateCategoryNameForExistingLogs(oldCategoryName: String, newCategoryName: String)
}

@Singleton
class WorkActivityRepositoryImpl @Inject constructor(
    private val workActivityDao: WorkActivityDao,
    private val workActivityComponentCrossRefDao: WorkActivityComponentCrossRefDao,
    private val workActivityTheBoyCrossRefDao: WorkActivityTheBoyCrossRefDao // Injected TheBoy CrossRef DAO
) : WorkActivityRepository {

    override suspend fun insertWorkActivity(log: WorkActivityLog, componentIds: List<Long>, theBoyIds: List<Long>): Long {
        val workActivityId = workActivityDao.insertLog(log)
        
        // Handle Component associations
        workActivityComponentCrossRefDao.deleteByWorkActivityId(workActivityId)
        if (componentIds.isNotEmpty()) {
            val componentCrossRefs = componentIds.map { componentId ->
                WorkActivityComponentCrossRef(workActivityId = workActivityId, componentId = componentId)
            }
            workActivityComponentCrossRefDao.insertAll(componentCrossRefs)
        }

        // Handle TheBoys associations
        workActivityTheBoyCrossRefDao.deleteByWorkActivityId(workActivityId)
        if (theBoyIds.isNotEmpty()) {
            val theBoyCrossRefs = theBoyIds.map { boyId ->
                WorkActivityTheBoyCrossRef(workActivityId = workActivityId, theBoyId = boyId)
            }
            workActivityTheBoyCrossRefDao.insertAll(theBoyCrossRefs)
        }
        return workActivityId
    }

    override fun getAllWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>> {
        return workActivityDao.getAllWorkActivitiesWithDetails()
    }

    override suspend fun getWorkActivityWithDetailsById(id: Long): WorkActivityDetails? {
        return workActivityDao.getWorkActivityWithDetailsById(id)
    }

    override suspend fun deleteLogById(id: Long) {
        // Cross-references are deleted by CASCADE constraint in DB
        workActivityDao.deleteLogById(id)
    }

    override suspend fun clearAllLogs() {
        // Cross-references are deleted by CASCADE constraint in DB
        workActivityDao.wipeDatabaseAndResetIds()
    }

    override fun getOngoingActivityByCategoryName(categoryName: String): Flow<WorkActivityDetails?> {
        return workActivityDao.getOngoingActivityWithDetailsByCategoryName(categoryName)
    }

    override fun getOngoingActivities(): Flow<List<WorkActivityLog>> {
        // TODO: Consider if this should also return Flow<List<WorkActivityDetails>> for consistency
        return workActivityDao.getOngoingActivities()
    }

    override fun getRecentWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>> { 
        return workActivityDao.getRecentWorkActivitiesWithDetails()
    }

    override suspend fun updateCategoryNameForExistingLogs(oldCategoryName: String, newCategoryName: String) {
        workActivityDao.updateCategoryNameForLogs(oldCategoryName, newCategoryName)
    }
}
