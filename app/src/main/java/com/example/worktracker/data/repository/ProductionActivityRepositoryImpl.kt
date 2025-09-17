package com.example.worktracker.data.repository

import android.util.Log
import com.example.worktracker.data.database.dao.ProductionActivityDao
import com.example.worktracker.data.database.entity.ProductionActivity
import com.example.worktracker.data.database.mappers.toFirestoreData
import com.example.worktracker.data.sync.IFirestoreSyncManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_REPO = "ProductionActivityRepo"
private const val PRODUCTION_ACTIVITIES_COLLECTION = "production_activities"

@Singleton
class ProductionActivityRepositoryImpl @Inject constructor(
    private val productionActivityDao: ProductionActivityDao,
    private val firestoreSyncManager: IFirestoreSyncManager,
    private val firebaseAuth: FirebaseAuth
) : ProductionActivityRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getAllProductionActivities(): Flow<List<ProductionActivity>> = productionActivityDao.getAllProductionActivities()

    override suspend fun getProductionActivityById(id: Long): ProductionActivity? = productionActivityDao.getProductionActivityById(id)

    override fun getProductionActivitiesByBoyId(boyId: Int): Flow<List<ProductionActivity>> = productionActivityDao.getProductionActivitiesByBoyId(boyId)

    override suspend fun insertProductionActivity(productionActivity: ProductionActivity): Long {
        val activityToInsert = productionActivity.copy(id = 0, lastModified = System.currentTimeMillis())
        val newId = productionActivityDao.insert(activityToInsert)
        
        val insertedActivity = activityToInsert.copy(id = newId)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.uploadEntity(
                    userId = userId,
                    collectionName = PRODUCTION_ACTIVITIES_COLLECTION,
                    entityId = newId.toString(),
                    data = insertedActivity.toFirestoreData()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync inserted ProductionActivity $newId to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync inserted ProductionActivity $newId")
        return newId
    }

    override suspend fun updateProductionActivity(productionActivity: ProductionActivity) {
        val activityToUpdate = productionActivity.copy(lastModified = System.currentTimeMillis())
        productionActivityDao.update(activityToUpdate)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.uploadEntity(
                    userId = userId,
                    collectionName = PRODUCTION_ACTIVITIES_COLLECTION,
                    entityId = activityToUpdate.id.toString(),
                    data = activityToUpdate.toFirestoreData()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync updated ProductionActivity ${activityToUpdate.id} to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync updated ProductionActivity ${activityToUpdate.id}")
    }

    override suspend fun deleteProductionActivity(productionActivity: ProductionActivity) {
        productionActivityDao.delete(productionActivity)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.deleteEntity(
                    userId = userId,
                    collectionName = PRODUCTION_ACTIVITIES_COLLECTION,
                    entityId = productionActivity.id.toString()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync deleted ProductionActivity ${productionActivity.id} to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync deleted ProductionActivity ${productionActivity.id}")
    }
}
