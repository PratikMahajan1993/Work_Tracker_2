package com.example.worktracker.data.repository

import com.example.worktracker.data.database.dao.OperatorInfoDao
import com.example.worktracker.data.database.entity.OperatorInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OperatorRepository @Inject constructor(
    private val operatorInfoDao: OperatorInfoDao
) {

    fun getAllOperators(): Flow<List<OperatorInfo>> = operatorInfoDao.getAllOperators()

    suspend fun getOperatorById(id: Int): OperatorInfo? = operatorInfoDao.getOperatorById(id)

    suspend fun insertOperator(operatorInfo: OperatorInfo) {
        operatorInfoDao.insert(operatorInfo)
    }

    suspend fun updateOperator(operatorInfo: OperatorInfo) {
        operatorInfoDao.update(operatorInfo)
    }

    suspend fun deleteOperator(operatorInfo: OperatorInfo) {
        operatorInfoDao.delete(operatorInfo)
    }
}
