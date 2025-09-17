package com.example.worktracker.workers // Or your chosen package e.g., com.example.worktracker.data.sync

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
import com.example.worktracker.data.database.mappers.toFirestoreData // Ensure this covers all entities or you have specific ones
import com.example.worktracker.data.sync.IFirestoreSyncManager
import com.example.worktracker.data.sync.ACTIVITY_CATEGORIES_COLLECTION
import com.example.worktracker.data.sync.COMPONENT_INFO_COLLECTION
import com.example.worktracker.data.sync.OPERATOR_INFO_COLLECTION
import com.example.worktracker.data.sync.PRODUCTION_ACTIVITIES_COLLECTION
import com.example.worktracker.data.sync.THE_BOYS_INFO_COLLECTION
import com.example.worktracker.data.sync.WORK_ACTIVITY_LOGS_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private const val TAG_UPLOAD_WORKER = "UploadAllDataWorker"

@HiltWorker
class UploadAllDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreSyncManager: IFirestoreSyncManager,
    private val firebaseAuth: FirebaseAuth,
    private val operatorInfoDao: OperatorInfoDao,
    private val workActivityDao: WorkActivityDao,
    private val activityCategoryDao: ActivityCategoryDao,
    private val theBoysInfoDao: TheBoysInfoDao,
    private val productionActivityDao: ProductionActivityDao,
    private val componentInfoDao: ComponentInfoDao
    // WorkActivityComponentCrossRefDao is implicitly used via WorkActivityDao methods
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG_UPLOAD_WORKER, "User not logged in. Cannot upload data.")
            // Consider Result.failure() or a specific error output if needed
            return Result.failure() 
        }
        // At this point, userId is smart-cast to String (non-null) due to the check above.
        // However, to explicitly address compiler errors, we'll use userId!! when passing.
        Log.i(TAG_UPLOAD_WORKER, "Starting full local data push to Firestore for user: $userId")

        return try {
            var allUploadsSuccessful = true

            withContext(Dispatchers.IO) {

                // 1. Upload OperatorInfo
                Log.d(TAG_UPLOAD_WORKER, "Uploading OperatorInfo...")
                try {
                    operatorInfoDao.getAllOperators().first().forEach { operator ->
                        val data = operator.toFirestoreData()
                        val result = firestoreSyncManager.uploadEntity(userId!!, OPERATOR_INFO_COLLECTION, operator.operatorId.toString(), data)
                        if (result.isFailure) {
                            Log.e(TAG_UPLOAD_WORKER, "Failed to upload Operator ${operator.operatorId}: ${result.exceptionOrNull()?.message}")
                            allUploadsSuccessful = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG_UPLOAD_WORKER, "Error uploading OperatorInfo: ${e.message}", e)
                    allUploadsSuccessful = false
                }

                // 2. Upload ActivityCategory
                Log.d(TAG_UPLOAD_WORKER, "Uploading ActivityCategory...")
                try {
                    activityCategoryDao.getAllCategories().first().forEach { category ->
                        val data = category.toFirestoreData()
                        // Firestore uses category.name as ID for ActivityCategory
                        val result = firestoreSyncManager.uploadEntity(userId!!, ACTIVITY_CATEGORIES_COLLECTION, category.name, data)
                        if (result.isFailure) {
                            Log.e(TAG_UPLOAD_WORKER, "Failed to upload Category ${category.name}: ${result.exceptionOrNull()?.message}")
                            allUploadsSuccessful = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG_UPLOAD_WORKER, "Error uploading ActivityCategory: ${e.message}", e)
                    allUploadsSuccessful = false
                }

                // 3. Upload TheBoysInfo
                Log.d(TAG_UPLOAD_WORKER, "Uploading TheBoysInfo...")
                try {
                    theBoysInfoDao.getAllTheBoys().first().forEach { boy ->
                        val data = boy.toFirestoreData()
                        val result = firestoreSyncManager.uploadEntity(userId!!, THE_BOYS_INFO_COLLECTION, boy.boyId.toString(), data)
                        if (result.isFailure) {
                            Log.e(TAG_UPLOAD_WORKER, "Failed to upload Boy ${boy.boyId}: ${result.exceptionOrNull()?.message}")
                            allUploadsSuccessful = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG_UPLOAD_WORKER, "Error uploading TheBoysInfo: ${e.message}", e)
                    allUploadsSuccessful = false
                }
                
                // 4. Upload ComponentInfo
                Log.d(TAG_UPLOAD_WORKER, "Uploading ComponentInfo...")
                try {
                    componentInfoDao.getAllComponents().first().forEach { component ->
                        val data = component.toFirestoreData()
                         // Firestore uses component.componentName as ID for ComponentInfo
                        val result = firestoreSyncManager.uploadEntity(userId!!, COMPONENT_INFO_COLLECTION, component.componentName, data)
                        if (result.isFailure) {
                            Log.e(TAG_UPLOAD_WORKER, "Failed to upload Component ${component.componentName}: ${result.exceptionOrNull()?.message}")
                            allUploadsSuccessful = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG_UPLOAD_WORKER, "Error uploading ComponentInfo: ${e.message}", e)
                    allUploadsSuccessful = false
                }

                // 5. Upload ProductionActivity
                Log.d(TAG_UPLOAD_WORKER, "Uploading ProductionActivity...")
                try {
                    productionActivityDao.getAllProductionActivities().first().forEach { activity ->
                        val data = activity.toFirestoreData()
                        val result = firestoreSyncManager.uploadEntity(userId!!, PRODUCTION_ACTIVITIES_COLLECTION, activity.id.toString(), data)
                        if (result.isFailure) {
                            Log.e(TAG_UPLOAD_WORKER, "Failed to upload ProductionActivity ${activity.id}: ${result.exceptionOrNull()?.message}")
                            allUploadsSuccessful = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG_UPLOAD_WORKER, "Error uploading ProductionActivity: ${e.message}", e)
                    allUploadsSuccessful = false
                }

                // 6. Upload WorkActivityLog (including componentIds)
                Log.d(TAG_UPLOAD_WORKER, "Uploading WorkActivityLog...")
                try {
                    workActivityDao.getAllWorkActivityLogs().first().forEach { log ->
                        val componentIds = try {
                            workActivityDao.getComponentIdsForWorkActivity(log.id).first() // Assuming this is a flow
                        } catch (e: Exception) {
                             Log.e(TAG_UPLOAD_WORKER, "Error fetching componentIds for log ${log.id}: ${e.message}", e)
                            emptyList<Long>() // Proceed with empty list if error
                        }
                        val firestoreData = log.toFirestoreData().toMutableMap()
                        firestoreData["componentIds"] = componentIds
                        val result = firestoreSyncManager.uploadEntity(userId!!, WORK_ACTIVITY_LOGS_COLLECTION, log.id.toString(), firestoreData)
                        if (result.isFailure) {
                            Log.e(TAG_UPLOAD_WORKER, "Failed to upload WorkActivityLog ${log.id}: ${result.exceptionOrNull()?.message}")
                            allUploadsSuccessful = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG_UPLOAD_WORKER, "Error uploading WorkActivityLog: ${e.message}", e)
                    allUploadsSuccessful = false
                }
            }

            if (allUploadsSuccessful) {
                Log.i(TAG_UPLOAD_WORKER, "All local data successfully scheduled for push to Firestore for user: $userId")
                Result.success()
            } else {
                Log.w(TAG_UPLOAD_WORKER, "Some local data failed to upload to Firestore for user: $userId. Check logs.")
                // Consider Result.retry() if you want WorkManager to attempt again later
                Result.failure() // Or Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG_UPLOAD_WORKER, "Critical error during full data upload process for user $userId", e)
            Result.failure()
        }
    }
}
