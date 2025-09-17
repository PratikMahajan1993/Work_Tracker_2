package com.example.worktracker.data.repository

import android.util.Log
import com.example.worktracker.data.database.dao.ActivityCategoryDao
import com.example.worktracker.data.database.entity.ActivityCategory
import com.example.worktracker.data.database.mappers.toFirestoreData
import com.example.worktracker.data.sync.IFirestoreSyncManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_REPO = "ActivityCategoryRepo"
private const val ACTIVITY_CATEGORIES_COLLECTION = "activity_categories"

@Singleton
class ActivityCategoryRepositoryImpl @Inject constructor(
    private val activityCategoryDao: ActivityCategoryDao,
    private val firestoreSyncManager: IFirestoreSyncManager, // Added
    private val firebaseAuth: FirebaseAuth // Added
) : ActivityCategoryRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getAllCategories(): Flow<List<ActivityCategory>> {
        return activityCategoryDao.getAllCategories()
    }

    override suspend fun insertCategory(category: ActivityCategory) {
        val categoryToInsert = category.copy(lastModified = System.currentTimeMillis())
        activityCategoryDao.insert(categoryToInsert)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.uploadEntity(
                    userId = userId,
                    collectionName = ACTIVITY_CATEGORIES_COLLECTION,
                    entityId = categoryToInsert.name, // Corrected: Using .name
                    data = categoryToInsert.toFirestoreData()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync inserted ActivityCategory '${categoryToInsert.name}' to Firestore: ${result.exceptionOrNull()?.message}") // Corrected
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync inserted ActivityCategory '${categoryToInsert.name}'") // Corrected
    }

    override suspend fun updateCategory(category: ActivityCategory) {
        val categoryToUpdate = category.copy(lastModified = System.currentTimeMillis())
        activityCategoryDao.update(categoryToUpdate)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.uploadEntity(
                    userId = userId,
                    collectionName = ACTIVITY_CATEGORIES_COLLECTION,
                    entityId = categoryToUpdate.name, // Corrected: Using .name
                    data = categoryToUpdate.toFirestoreData()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync updated ActivityCategory '${categoryToUpdate.name}' to Firestore: ${result.exceptionOrNull()?.message}") // Corrected
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync updated ActivityCategory '${categoryToUpdate.name}'") // Corrected
    }

    override suspend fun deleteCategory(category: ActivityCategory) {
        activityCategoryDao.delete(category)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.deleteEntity(
                    userId = userId,
                    collectionName = ACTIVITY_CATEGORIES_COLLECTION,
                    entityId = category.name // Corrected: Using .name
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync deleted ActivityCategory '${category.name}' to Firestore: ${result.exceptionOrNull()?.message}") // Corrected
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync deleted ActivityCategory '${category.name}'") // Corrected
    }

    override suspend fun getCategoryByName(name: String): ActivityCategory? {
        return activityCategoryDao.getCategoryByName(name)
    }
}
