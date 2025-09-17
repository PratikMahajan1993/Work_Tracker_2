package com.example.worktracker.data.repository

import android.util.Log
import com.example.worktracker.data.database.dao.WorkActivityDao
import com.example.worktracker.data.database.dao.WorkActivityComponentCrossRefDao
// Removed: import com.example.worktracker.data.database.dao.WorkActivityTheBoyCrossRefDao
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.database.entity.WorkActivityComponentCrossRef
// Removed: import com.example.worktracker.data.database.entity.WorkActivityTheBoyCrossRef
import com.example.worktracker.data.database.mappers.toFirestoreData
import com.example.worktracker.data.database.relation.WorkActivityDetails
import com.example.worktracker.data.sync.IFirestoreSyncManager
import com.example.worktracker.data.sync.WORK_ACTIVITY_LOGS_COLLECTION // Import constant
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_REPO = "WorkActivityRepo"
// private const val WORK_ACTIVITY_LOGS_COLLECTION = "work_activity_logs" // Now imported

@Singleton
class WorkActivityRepositoryImpl @Inject constructor(
    private val workActivityDao: WorkActivityDao,
    private val workActivityComponentCrossRefDao: WorkActivityComponentCrossRefDao,
    // Removed: private val workActivityTheBoyCrossRefDao: WorkActivityTheBoyCrossRefDao,
    private val firestoreSyncManager: IFirestoreSyncManager, 
    private val firebaseAuth: FirebaseAuth
) : WorkActivityRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override suspend fun insertWorkActivity(log: WorkActivityLog, componentIds: List<Long>): Long {
        val workActivityId = workActivityDao.insertLog(log) 
        
        // Handle Component associations
        workActivityComponentCrossRefDao.deleteByWorkActivityId(workActivityId)
        if (componentIds.isNotEmpty()) {
            val componentCrossRefs = componentIds.map { componentId ->
                WorkActivityComponentCrossRef(workActivityId = workActivityId, componentId = componentId)
            }
            workActivityComponentCrossRefDao.insertAll(componentCrossRefs)
        }

        // Removed TheBoys associations logic

        // Sync to Firestore
        firebaseAuth.currentUser?.uid?.let { userId ->
            val logToSync = log.copy(id = workActivityId, lastModified = System.currentTimeMillis())
            
            // Prepare data for Firestore, including componentIds
            val firestoreData = logToSync.toFirestoreData().toMutableMap()
            firestoreData["componentIds"] = componentIds // Add componentIds to the map
            
            repositoryScope.launch {
                val result = firestoreSyncManager.uploadEntity(
                    userId = userId,
                    collectionName = WORK_ACTIVITY_LOGS_COLLECTION,
                    entityId = workActivityId.toString(),
                    data = firestoreData // Use the augmented map
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync inserted WorkActivityLog $workActivityId to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync inserted WorkActivityLog $workActivityId")
        
        return workActivityId
    }

    override fun getAllWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>> {
        return workActivityDao.getAllWorkActivitiesWithDetails()
    }

    override suspend fun getWorkActivityWithDetailsById(id: Long): WorkActivityDetails? {
        return workActivityDao.getWorkActivityWithDetailsById(id)
    }

    override suspend fun deleteLogById(id: Long) {
        workActivityDao.deleteLogById(id) 
        // Also delete related component cross references (Room handles this if FKs have cascade delete, but explicit is safer here)
        workActivityComponentCrossRefDao.deleteByWorkActivityId(id)
        
        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.deleteEntity(
                    userId = userId,
                    collectionName = WORK_ACTIVITY_LOGS_COLLECTION,
                    entityId = id.toString()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync deleted WorkActivityLog $id to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync deleted WorkActivityLog $id")
    }

    override suspend fun clearAllLogs() {
        workActivityDao.wipeDatabaseAndResetIds() 
        // For Firestore, rely on deleteAllUserData in FirestoreSyncManager called elsewhere, or implement specific collection clearing.
        // If called here, ensure user is logged in and it only clears this user's work_activity_logs.
        firebaseAuth.currentUser?.uid?.let {
            // For now, let global sync or manual Firestore management handle this to avoid accidental mass delete here.
            Log.w(TAG_REPO, "Clearing $WORK_ACTIVITY_LOGS_COLLECTION in Firestore for user is typically handled by a broader sync/delete strategy, not directly in clearAllLogs().")
        } 
    }

    override fun getOngoingActivityByCategoryName(categoryName: String): Flow<WorkActivityDetails?> {
        return workActivityDao.getOngoingActivityWithDetailsByCategoryName(categoryName)
    }

    override fun getOngoingActivities(): Flow<List<WorkActivityLog>> {
        return workActivityDao.getOngoingActivities()
    }

    override fun getRecentWorkActivitiesWithDetails(): Flow<List<WorkActivityDetails>> { 
        return workActivityDao.getRecentWorkActivitiesWithDetails()
    }

    override suspend fun updateCategoryNameForExistingLogs(oldCategoryName: String, newCategoryName: String) {
        workActivityDao.updateCategoryNameForLogs(oldCategoryName, newCategoryName)
        Log.w(TAG_REPO, "Firestore sync for updateCategoryNameForExistingLogs not yet implemented.")
    }
}
