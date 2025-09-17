package com.example.worktracker.data.repository

import com.example.worktracker.data.database.entity.TheBoysInfo
import kotlinx.coroutines.flow.Flow

interface TheBoysRepository {
    fun getAllTheBoys(): Flow<List<TheBoysInfo>>
    suspend fun getTheBoyById(id: Int): TheBoysInfo?
    suspend fun insertTheBoy(theBoyInfo: TheBoysInfo)
    suspend fun updateTheBoy(theBoyInfo: TheBoysInfo)
    suspend fun deleteTheBoy(theBoyInfo: TheBoysInfo)
}
