package com.example.worktracker.data.repository

import android.util.Log
import com.example.worktracker.data.database.dao.TheBoysInfoDao
import com.example.worktracker.data.database.entity.TheBoysInfo
import com.example.worktracker.data.database.mappers.toFirestoreData
import com.example.worktracker.data.sync.IFirestoreSyncManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_REPO = "TheBoysRepo"
private const val THE_BOYS_INFO_COLLECTION = "the_boys_info"

@Singleton
class TheBoysRepositoryImpl @Inject constructor(
    private val theBoysInfoDao: TheBoysInfoDao,
    private val firestoreSyncManager: IFirestoreSyncManager,
    private val firebaseAuth: FirebaseAuth
) : TheBoysRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getAllTheBoys(): Flow<List<TheBoysInfo>> = theBoysInfoDao.getAllTheBoys()

    override suspend fun getTheBoyById(id: Int): TheBoysInfo? = theBoysInfoDao.getTheBoyById(id)

    override suspend fun insertTheBoy(theBoyInfo: TheBoysInfo) {
        val boyToInsert = theBoyInfo.copy(lastModified = System.currentTimeMillis())
        theBoysInfoDao.insert(boyToInsert)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.uploadEntity(
                    userId = userId,
                    collectionName = THE_BOYS_INFO_COLLECTION,
                    entityId = boyToInsert.boyId.toString(),
                    data = boyToInsert.toFirestoreData()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync inserted TheBoysInfo '${boyToInsert.boyId}' to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync inserted TheBoysInfo '${boyToInsert.boyId}'")
    }

    override suspend fun updateTheBoy(theBoyInfo: TheBoysInfo) {
        val boyToUpdate = theBoyInfo.copy(lastModified = System.currentTimeMillis())
        theBoysInfoDao.update(boyToUpdate)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.uploadEntity(
                    userId = userId,
                    collectionName = THE_BOYS_INFO_COLLECTION,
                    entityId = boyToUpdate.boyId.toString(),
                    data = boyToUpdate.toFirestoreData()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync updated TheBoysInfo '${boyToUpdate.boyId}' to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync updated TheBoysInfo '${boyToUpdate.boyId}'")
    }

    override suspend fun deleteTheBoy(theBoyInfo: TheBoysInfo) {
        theBoysInfoDao.delete(theBoyInfo)

        firebaseAuth.currentUser?.uid?.let { userId ->
            repositoryScope.launch {
                val result = firestoreSyncManager.deleteEntity(
                    userId = userId,
                    collectionName = THE_BOYS_INFO_COLLECTION,
                    entityId = theBoyInfo.boyId.toString()
                )
                if (result.isFailure) {
                    Log.e(TAG_REPO, "Failed to sync deleted TheBoysInfo '${theBoyInfo.boyId}' to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        } ?: Log.w(TAG_REPO, "User not logged in, cannot sync deleted TheBoysInfo '${theBoyInfo.boyId}'")
    }
}
