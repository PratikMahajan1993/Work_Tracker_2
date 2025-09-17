package com.example.worktracker.data.repository

import android.util.Log
import com.example.worktracker.data.database.dao.OperatorInfoDao
import com.example.worktracker.data.database.entity.OperatorInfo
import com.example.worktracker.data.database.mappers.toFirestoreData
import com.example.worktracker.data.sync.IFirestoreSyncManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_REPO = "OperatorRepo"
private const val OPERATOR_INFO_COLLECTION = "operator_info"

@Singleton
class OperatorRepositoryImpl @Inject constructor(
    private val operatorInfoDao: OperatorInfoDao,
    private val firestoreSyncManager: IFirestoreSyncManager,
    private val firebaseAuth: FirebaseAuth
) : OperatorRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getAllOperators(): Flow<List<OperatorInfo>> = operatorInfoDao.getAllOperators()

    override suspend fun getOperatorById(id: Int): OperatorInfo? = operatorInfoDao.getOperatorById(id)

    override suspend fun insertOperator(operatorInfo: OperatorInfo) {
        val operatorToInsert = operatorInfo.copy(lastModified = System.currentTimeMillis())
        val newId = operatorInfoDao.insert(operatorToInsert) // Capture the new ID

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.uploadEntity(
                    userId = userId,
                    collectionName = OPERATOR_INFO_COLLECTION,
                    entityId = newId.toString(), // Use the new ID for Firestore
                    data = operatorToInsert.copy(operatorId = newId.toInt()).toFirestoreData()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync inserted OperatorInfo $newId to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync inserted OperatorInfo $newId")
    }

    override suspend fun updateOperator(operatorInfo: OperatorInfo) {
        val operatorToUpdate = operatorInfo.copy(lastModified = System.currentTimeMillis())
        operatorInfoDao.update(operatorToUpdate)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.uploadEntity(
                    userId = userId,
                    collectionName = OPERATOR_INFO_COLLECTION,
                    entityId = operatorToUpdate.operatorId.toString(),
                    data = operatorToUpdate.toFirestoreData()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync updated OperatorInfo ${operatorToUpdate.operatorId} to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync updated OperatorInfo ${operatorToUpdate.operatorId}")
    }

    override suspend fun deleteOperator(operatorInfo: OperatorInfo) {
        operatorInfoDao.delete(operatorInfo)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.deleteEntity(
                    userId = userId,
                    collectionName = OPERATOR_INFO_COLLECTION,
                    entityId = operatorInfo.operatorId.toString()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync deleted OperatorInfo ${operatorInfo.operatorId} to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync deleted OperatorInfo ${operatorInfo.operatorId}")
    }
}
