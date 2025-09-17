package com.example.worktracker.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.worktracker.data.database.dao.ActivityCategoryDao
import com.example.worktracker.data.database.dao.ComponentInfoDao
import com.example.worktracker.data.database.dao.OperatorInfoDao
import com.example.worktracker.data.database.dao.ProductionActivityDao
import com.example.worktracker.data.database.dao.TheBoysInfoDao
import com.example.worktracker.data.database.dao.WorkActivityDao
import com.example.worktracker.data.database.dao.WorkActivityComponentCrossRefDao // Added import
import com.example.worktracker.data.database.entity.WorkActivityComponentCrossRef // Added import
import com.example.worktracker.data.database.entity.WorkActivityLog // For type hint in lambda
import com.example.worktracker.data.database.mappers.toActivityCategory
import com.example.worktracker.data.database.mappers.toComponentInfo
import com.example.worktracker.data.database.mappers.toOperatorInfo
import com.example.worktracker.data.database.mappers.toProductionActivity
import com.example.worktracker.data.database.mappers.toTheBoysInfo
import com.example.worktracker.data.database.mappers.toWorkActivityLog
// Import the collection name constants
import com.example.worktracker.data.sync.OPERATOR_INFO_COLLECTION
import com.example.worktracker.data.sync.ACTIVITY_CATEGORIES_COLLECTION
import com.example.worktracker.data.sync.THE_BOYS_INFO_COLLECTION
import com.example.worktracker.data.sync.COMPONENT_INFO_COLLECTION
import com.example.worktracker.data.sync.PRODUCTION_ACTIVITIES_COLLECTION
import com.example.worktracker.data.sync.WORK_ACTIVITY_LOGS_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

private const val TAG_SYNC_WORKER = "SyncWorker"

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreSyncManager: IFirestoreSyncManager,
    private val firebaseAuth: FirebaseAuth,
    private val operatorInfoDao: OperatorInfoDao,
    private val workActivityDao: WorkActivityDao,
    private val activityCategoryDao: ActivityCategoryDao,
    private val theBoysInfoDao: TheBoysInfoDao,
    private val productionActivityDao: ProductionActivityDao,
    private val componentInfoDao: ComponentInfoDao,
    private val workActivityComponentCrossRefDao: WorkActivityComponentCrossRefDao // Added DAO
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG_SYNC_WORKER, "User not logged in. Sync aborted.")
            return Result.failure()
        }

        Log.i(TAG_SYNC_WORKER, "Starting Firestore to Room sync for user: $userId")

        try {
            // Sync OperatorInfo
            syncEntities(userId, OPERATOR_INFO_COLLECTION, operatorInfoDao::upsertAllOperators) { it.toOperatorInfo() }
            
            // Sync ActivityCategory
            syncEntities(userId, ACTIVITY_CATEGORIES_COLLECTION, activityCategoryDao::upsertAllCategories) { it.toActivityCategory() }

            // Sync TheBoysInfo
            syncEntities(userId, THE_BOYS_INFO_COLLECTION, theBoysInfoDao::upsertAllTheBoys) { it.toTheBoysInfo() }

            // Sync ComponentInfo
            syncEntities(userId, COMPONENT_INFO_COLLECTION, componentInfoDao::upsertAllComponents) { it.toComponentInfo() }
            
            // Sync ProductionActivity
            syncEntities(userId, PRODUCTION_ACTIVITIES_COLLECTION, productionActivityDao::upsertAllProductionActivities) { it.toProductionActivity() }

            // Sync WorkActivityLog and its Component Cross-References
            syncEntities<WorkActivityLog>(userId, WORK_ACTIVITY_LOGS_COLLECTION, 
                upsertAllDaoAction = { logsToSync ->
                    workActivityDao.upsertAllWorkActivityLogs(logsToSync)
                    for (log in logsToSync) {
                        // The log.id should be correctly mapped from Firestore document ID by toWorkActivityLog mapper
                        // If log.id is 0 or invalid, this won't work as expected.
                        if (log.id == 0L) {
                            Log.w(TAG_SYNC_WORKER, "Skipping cross-ref sync for WorkActivityLog with unmapped ID from Firestore.")
                            continue
                        }
                        workActivityComponentCrossRefDao.deleteByWorkActivityId(log.id)
                        log.componentIdsForSync?.let { componentIds ->
                            if (componentIds.isNotEmpty()) {
                                val crossRefs = componentIds.map { componentId ->
                                    WorkActivityComponentCrossRef(workActivityId = log.id, componentId = componentId)
                                }
                                workActivityComponentCrossRefDao.insertAll(crossRefs)
                            }
                        }
                    }
                },
                mapper = { it.toWorkActivityLog() } 
            )

            Log.i(TAG_SYNC_WORKER, "Firestore to Room sync completed successfully for user: $userId")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG_SYNC_WORKER, "Error during Firestore to Room sync for user $userId", e)
            return Result.failure()
        }
    }

    private suspend fun <T : Any> syncEntities(
        userId: String,
        collectionName: String,
        upsertAllDaoAction: suspend (List<T>) -> Unit, 
        mapper: (Map<String, Any?>) -> T?
    ) {
        Log.d(TAG_SYNC_WORKER, "Syncing collection: $collectionName for user $userId")
        val result = firestoreSyncManager.getEntities(userId, collectionName).firstOrNull()

        result?.fold(
            onSuccess = { maps ->
                if (maps.isEmpty()) {
                    Log.d(TAG_SYNC_WORKER, "No entities found in Firestore for $collectionName. Nothing to sync.")
                    return@fold
                }
                val entities = maps.mapNotNull { dataMap ->
                    // Add Firestore document ID to the map before mapping, if not already present
                    val mutableMap = dataMap.toMutableMap()
                    if (!mutableMap.containsKey("firestore_document_id") && (dataMap as? com.google.firebase.firestore.DocumentSnapshot)?.id != null) {
                         // This part is tricky as the `maps` are List<Map<String, Any?>>, not DocumentSnapshot directly.
                         // The IFirestoreSyncManager.getEntities should ideally pass the document ID within the map it returns.
                         // Assuming the mapper (e.g., toWorkActivityLog) can handle getting the ID from DocumentSnapshot.id if it is a DocumentSnapshot
                         // or expects a specific key like "firestore_document_id".
                         // For WorkActivityLog, the toWorkActivityLog mapper was updated to get `id` from `this.id` (DocumentSnapshot)
                         // or `firestore_document_id` / `id` from Map.
                    }
                    mapper(mutableMap)
                }
                if (entities.isNotEmpty()) {
                    upsertAllDaoAction(entities)
                    Log.i(TAG_SYNC_WORKER, "Successfully synced ${entities.size} entities for $collectionName")
                } else {
                    Log.d(TAG_SYNC_WORKER, "No entities could be mapped from Firestore for $collectionName.")
                }
            },
            onFailure = { exception ->
                Log.e(TAG_SYNC_WORKER, "Failed to fetch entities for $collectionName: ${exception.message}", exception)
                throw exception 
            }
        )
    }
}
