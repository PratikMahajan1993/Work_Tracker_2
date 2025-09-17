package com.example.worktracker.data.repository

import android.util.Log
import com.example.worktracker.data.database.dao.ComponentInfoDao
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.database.mappers.toFirestoreData
import com.example.worktracker.data.sync.IFirestoreSyncManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_REPO = "ComponentInfoRepo"
private const val COMPONENT_INFO_COLLECTION = "component_info"

@Singleton // Added Singleton annotation as it's provided as Singleton in AppModule
class ComponentInfoRepositoryImpl @Inject constructor(
    private val componentInfoDao: ComponentInfoDao,
    private val firestoreSyncManager: IFirestoreSyncManager, // Added
    private val firebaseAuth: FirebaseAuth // Added
) : ComponentInfoRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getAllComponents(): Flow<List<ComponentInfo>> {
        return componentInfoDao.getAllComponents()
    }

    override fun getComponentById(id: Long): Flow<ComponentInfo?> {
        return componentInfoDao.getComponentById(id)
    }

    override suspend fun insertComponent(componentInfo: ComponentInfo): Result<Unit> {
        val componentToInsert = componentInfo.copy(lastModified = System.currentTimeMillis())
        val existingComponent = componentInfoDao.getComponentByName(componentToInsert.componentName)
        return if (existingComponent == null) {
            try {
                componentInfoDao.insert(componentToInsert)
                // Sync to Firestore after successful Room insert
                firebaseAuth.currentUser?.uid?.let { userId ->
                    repositoryScope.launch {
                        val result = firestoreSyncManager.uploadEntity(
                            userId = userId,
                            collectionName = COMPONENT_INFO_COLLECTION,
                            entityId = componentToInsert.componentName, // Using componentName as ID
                            data = componentToInsert.toFirestoreData()
                        )
                        if (result.isFailure) {
                            Log.e(TAG_REPO, "Failed to sync inserted ComponentInfo '${componentToInsert.componentName}' to Firestore: ${result.exceptionOrNull()?.message}")
                        }
                    }
                } ?: Log.w(TAG_REPO, "User not logged in, cannot sync inserted ComponentInfo '${componentToInsert.componentName}'")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG_REPO, "Error inserting ComponentInfo '${componentToInsert.componentName}' into Room: ${e.message}", e)
                Result.failure(Exception("Failed to insert component: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Component name '${componentToInsert.componentName}' already exists."))
        }
    }

    override suspend fun updateComponent(componentInfo: ComponentInfo): Result<Unit> {
        val componentToUpdate = componentInfo.copy(lastModified = System.currentTimeMillis())
        val existingComponentByName = componentInfoDao.getComponentByName(componentToUpdate.componentName)
        return if (existingComponentByName == null || existingComponentByName.id == componentToUpdate.id) {
            try {
                componentInfoDao.update(componentToUpdate)
                // Sync to Firestore after successful Room update
                firebaseAuth.currentUser?.uid?.let { userId ->
                    repositoryScope.launch {
                        val result = firestoreSyncManager.uploadEntity(
                            userId = userId,
                            collectionName = COMPONENT_INFO_COLLECTION,
                            entityId = componentToUpdate.componentName, // Using componentName as ID
                            data = componentToUpdate.toFirestoreData()
                        )
                        if (result.isFailure) {
                            Log.e(TAG_REPO, "Failed to sync updated ComponentInfo '${componentToUpdate.componentName}' to Firestore: ${result.exceptionOrNull()?.message}")
                        }
                    }
                } ?: Log.w(TAG_REPO, "User not logged in, cannot sync updated ComponentInfo '${componentToUpdate.componentName}'")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG_REPO, "Error updating ComponentInfo '${componentToUpdate.componentName}' in Room: ${e.message}", e)
                 Result.failure(Exception("Failed to update component: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Cannot update: Component name '${componentToUpdate.componentName}' already exists for another component."))
        }
    }

    override suspend fun deleteComponent(componentInfo: ComponentInfo) {
        componentInfoDao.delete(componentInfo)
        // Sync deletion to Firestore
        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.deleteEntity(
                    userId = userId,
                    collectionName = COMPONENT_INFO_COLLECTION,
                    entityId = componentInfo.componentName // Using componentName as ID
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync deleted ComponentInfo '${componentInfo.componentName}' to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync deleted ComponentInfo '${componentInfo.componentName}'")
    }

    override suspend fun getComponentByName(name: String): ComponentInfo? {
        return componentInfoDao.getComponentByName(name)
    }
}
