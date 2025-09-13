package com.example.worktracker.data.repository

import com.example.worktracker.data.database.dao.TheBoysInfoDao
import com.example.worktracker.data.database.entity.TheBoysInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface TheBoysRepository {
    fun getAllTheBoys(): Flow<List<TheBoysInfo>>
    suspend fun getTheBoyById(id: Int): TheBoysInfo?
    suspend fun insertTheBoy(theBoyInfo: TheBoysInfo)
    suspend fun updateTheBoy(theBoyInfo: TheBoysInfo)
    suspend fun deleteTheBoy(theBoyInfo: TheBoysInfo)
}

@Singleton
class TheBoysRepositoryImpl @Inject constructor(
    private val theBoysInfoDao: TheBoysInfoDao
) : TheBoysRepository {

    override fun getAllTheBoys(): Flow<List<TheBoysInfo>> = theBoysInfoDao.getAllTheBoys()

    override suspend fun getTheBoyById(id: Int): TheBoysInfo? = theBoysInfoDao.getTheBoyById(id)

    override suspend fun insertTheBoy(theBoyInfo: TheBoysInfo) {
        theBoysInfoDao.insert(theBoyInfo)
    }

    override suspend fun updateTheBoy(theBoyInfo: TheBoysInfo) {
        theBoysInfoDao.update(theBoyInfo)
    }

    override suspend fun deleteTheBoy(theBoyInfo: TheBoysInfo) {
        theBoysInfoDao.delete(theBoyInfo)
    }
}
