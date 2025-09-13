package com.example.worktracker.data.repository

import com.example.worktracker.data.database.dao.ComponentInfoDao
import com.example.worktracker.data.database.entity.ComponentInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ComponentInfoRepositoryImpl @Inject constructor(
    private val componentInfoDao: ComponentInfoDao
) : ComponentInfoRepository {

    override fun getAllComponents(): Flow<List<ComponentInfo>> {
        return componentInfoDao.getAllComponents()
    }

    override fun getComponentById(id: Long): Flow<ComponentInfo?> {
        return componentInfoDao.getComponentById(id)
    }

    override suspend fun insertComponent(componentInfo: ComponentInfo): Result<Unit> {
        // Check for name uniqueness before inserting
        val existingComponent = componentInfoDao.getComponentByName(componentInfo.componentName)
        return if (existingComponent == null) {
            try {
                componentInfoDao.insert(componentInfo)
                Result.success(Unit)
            } catch (e: Exception) {
                // Catch potential SQLiteConstraintException if index somehow fails (though ABORT should handle it)
                Result.failure(Exception("Failed to insert component: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Component name '${componentInfo.componentName}' already exists."))
        }
    }

    override suspend fun updateComponent(componentInfo: ComponentInfo): Result<Unit> {
        // Check if the name is being changed to one that already exists (excluding itself)
        val existingComponentByName = componentInfoDao.getComponentByName(componentInfo.componentName)
        return if (existingComponentByName == null || existingComponentByName.id == componentInfo.id) {
            try {
                componentInfoDao.update(componentInfo)
                Result.success(Unit)
            } catch (e: Exception) {
                 Result.failure(Exception("Failed to update component: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Cannot update: Component name '${componentInfo.componentName}' already exists for another component."))
        }
    }

    override suspend fun deleteComponent(componentInfo: ComponentInfo) {
        componentInfoDao.delete(componentInfo)
    }

    override suspend fun getComponentByName(name: String): ComponentInfo? {
        return componentInfoDao.getComponentByName(name)
    }
}
