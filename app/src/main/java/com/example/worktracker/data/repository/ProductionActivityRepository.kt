package com.example.worktracker.data.repository

import com.example.worktracker.data.database.entity.ProductionActivity
import kotlinx.coroutines.flow.Flow

interface ProductionActivityRepository {
    fun getAllProductionActivities(): Flow<List<ProductionActivity>>
    suspend fun getProductionActivityById(id: Long): ProductionActivity?
    fun getProductionActivitiesByBoyId(boyId: Int): Flow<List<ProductionActivity>>
    suspend fun insertProductionActivity(productionActivity: ProductionActivity): Long
    suspend fun updateProductionActivity(productionActivity: ProductionActivity)
    suspend fun deleteProductionActivity(productionActivity: ProductionActivity)
}
