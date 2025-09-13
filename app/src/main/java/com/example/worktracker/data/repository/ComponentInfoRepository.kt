package com.example.worktracker.data.repository

import com.example.worktracker.data.database.entity.ComponentInfo
import kotlinx.coroutines.flow.Flow

interface ComponentInfoRepository {
    fun getAllComponents(): Flow<List<ComponentInfo>>
    fun getComponentById(id: Long): Flow<ComponentInfo?>
    suspend fun insertComponent(componentInfo: ComponentInfo): Result<Unit>
    suspend fun updateComponent(componentInfo: ComponentInfo): Result<Unit>
    suspend fun deleteComponent(componentInfo: ComponentInfo)
    suspend fun getComponentByName(name: String): ComponentInfo?
}
