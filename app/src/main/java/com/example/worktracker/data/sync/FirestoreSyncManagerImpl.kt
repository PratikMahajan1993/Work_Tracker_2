package com.example.worktracker.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.* // KTX extensions for Firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch // Import for the catch operator
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "FirestoreSyncManager"

// Define collection names as public constants
const val WORK_ACTIVITY_LOGS_COLLECTION = "work_activity_logs"
const val OPERATOR_INFO_COLLECTION = "operator_info"
const val ACTIVITY_CATEGORIES_COLLECTION = "activity_categories"
const val THE_BOYS_INFO_COLLECTION = "the_boys_info"
const val PRODUCTION_ACTIVITIES_COLLECTION = "production_activities"
const val COMPONENT_INFO_COLLECTION = "component_info"

// List using the public constants for deleteAllUserData
private val USER_DATA_COLLECTIONS = listOf(
    WORK_ACTIVITY_LOGS_COLLECTION,
    OPERATOR_INFO_COLLECTION,
    ACTIVITY_CATEGORIES_COLLECTION,
    THE_BOYS_INFO_COLLECTION,
    PRODUCTION_ACTIVITIES_COLLECTION,
    COMPONENT_INFO_COLLECTION
)

class FirestoreSyncManagerImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : IFirestoreSyncManager {

    override suspend fun uploadEntity(
        userId: String,
        collectionName: String,
        entityId: String,
        data: Map<String, Any?>
    ): Result<Unit> {
        return try {
            Log.d(TAG, "Uploading entity: userId=$userId, collection=$collectionName, entityId=$entityId")
            firestore.collection("users").document(userId)
                .collection(collectionName).document(entityId)
                .set(data)
                .await()
            Log.d(TAG, "Upload successful for entityId: $entityId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading entity: $entityId to $collectionName for user $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteEntity(
        userId: String,
        collectionName: String,
        entityId: String
    ): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting entity: userId=$userId, collection=$collectionName, entityId=$entityId")
            firestore.collection("users").document(userId)
                .collection(collectionName).document(entityId)
                .delete()
                .await()
            Log.d(TAG, "Deletion successful for entityId: $entityId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting entity: $entityId from $collectionName for user $userId", e)
            Result.failure(e)
        }
    }

    override fun getEntities(
        userId: String,
        collectionName: String
    ): Flow<Result<List<Map<String, Any?>>>> = flow {
        Log.d(TAG, "Fetching entities: userId=$userId, collection=$collectionName")
        val snapshot = firestore.collection("users").document(userId)
            .collection(collectionName)
            .get()
            .await()
        
        val documents = snapshot.documents.mapNotNull { document ->
            document.data?.toMutableMap()?.apply {
                put("firestore_document_id", document.id) 
            }
        }
        Log.d(TAG, "Fetched ${documents.size} entities from $collectionName for user $userId")
        emit(Result.success(documents))
    }.catch { e -> // Apply the catch operator to the flow
        Log.e(TAG, "Error in getEntities flow for $collectionName, user $userId", e)
        // Do not check for internal AbortFlowException here.
        // Simply emit the failure for any exception caught.
        emit(Result.failure(e))
    }

    override suspend fun deleteAllUserData(userId: String): Result<Unit> {
        Log.d(TAG, "Attempting to delete all user data for userId: $userId")
        return try {
            val userDocRef = firestore.collection("users").document(userId)
            
            for (collectionName in USER_DATA_COLLECTIONS) {
                Log.d(TAG, "Deleting documents from collection: $collectionName for user $userId")
                var lastDocumentSnapshot: com.google.firebase.firestore.DocumentSnapshot? = null
                do {
                    val query = userDocRef.collection(collectionName).limit(500)
                    val querySnapshot = (if (lastDocumentSnapshot == null) query else query.startAfter(lastDocumentSnapshot)).get().await()
                    
                    if (querySnapshot.isEmpty) {
                        Log.d(TAG, "No more documents found in $collectionName for user $userId to delete in this batch.")
                        break
                    }
                    
                    val batch = firestore.batch()
                    querySnapshot.documents.forEach { documentSnapshot ->
                        batch.delete(documentSnapshot.reference)
                    }
                    batch.commit().await()
                    Log.d(TAG, "Successfully deleted batch of ${querySnapshot.size()} documents from $collectionName for user $userId")
                    lastDocumentSnapshot = querySnapshot.documents.lastOrNull()
                } while (querySnapshot.size() >= 500) 
            }
            Log.i(TAG, "Successfully processed deletion for all specified user data collections for userId: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all user data for $userId", e)
            Result.failure(e)
        }
    }
}
