package com.example.worktracker.data.sync

import kotlinx.coroutines.flow.Flow

interface IFirestoreSyncManager {
    /**
     * Uploads a single entity (represented as a Map) to a specified collection in Firestore.
     * The entityId will be used as the document ID in Firestore.
     */
    suspend fun uploadEntity(
        userId: String,
        collectionName: String,
        entityId: String,
        data: Map<String, Any?>
    ): Result<Unit>

    /**
     * Deletes a single entity from a specified collection in Firestore using its entityId (document ID).
     */
    suspend fun deleteEntity(
        userId: String,
        collectionName: String,
        entityId: String
    ): Result<Unit>

    /**
     * Fetches all entities (as Maps) from a specified collection for a given user.
     * Returns a Flow to observe changes if needed, though for periodic sync, a one-time fetch might be used internally.
     */
    fun getEntities( // Changed to return Flow<Result<List<Map<String, Any?>>>> for better error handling and structure
        userId: String,
        collectionName: String
    ): Flow<Result<List<Map<String, Any?>>>> 

    /**
     * Deletes all data associated with a specific userId in Firestore.
     * This needs to be implemented carefully, often by deleting a top-level user document
     * and then recursively deleting its subcollections if that's the data structure.
     */
    suspend fun deleteAllUserData(userId: String): Result<Unit>
}
