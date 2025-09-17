package com.example.worktracker.data.repository

import com.example.worktracker.data.database.entity.OperatorInfo
import kotlinx.coroutines.flow.Flow

interface OperatorRepository {
    fun getAllOperators(): Flow<List<OperatorInfo>>
    suspend fun getOperatorById(id: Int): OperatorInfo?
    suspend fun insertOperator(operatorInfo: OperatorInfo)
    suspend fun updateOperator(operatorInfo: OperatorInfo)
    suspend fun deleteOperator(operatorInfo: OperatorInfo)
}
